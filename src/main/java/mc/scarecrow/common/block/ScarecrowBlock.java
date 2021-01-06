package mc.scarecrow.common.block;

import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.capability.ScarecrowCapabilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScarecrowBlock extends Block {

    private static final Logger LOGGER = LogManager.getLogger();

    public ScarecrowBlock() {
        super(Properties
                .create(Material.IRON, MaterialColor.GRAY)
                .sound(SoundType.STONE)
                .hardnessAndResistance(1.5f, 6)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return hasTileEntity(state) ? new ScarecrowTile() : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof INamedContainerProvider) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        try {
            if (!worldIn.isRemote()) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);

                if (tileEntity instanceof ScarecrowTile) {
                    ServerWorld serverWorld = (ServerWorld) worldIn;
                    // Register into capabilities to keep chunk loaded
                    worldIn.getCapability(ScarecrowCapabilities.CHUNK_CAPABILITY).ifPresent(tracker -> {
                        ChunkPos chunkPos = serverWorld.getChunk(pos).getPos();
                        tracker.add(chunkPos, pos);
                    });
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state) {
        super.onPlayerDestroy(worldIn, pos, state);
        onRemovedFromWorld((World) worldIn, pos);
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        super.onExplosionDestroy(worldIn, pos, explosionIn);
        onRemovedFromWorld(worldIn, pos);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        onRemovedFromWorld(worldIn, pos);
    }

    private void onRemovedFromWorld(World worldIn, BlockPos pos) {
        if (!worldIn.isRemote()) {
            if (worldIn.getBlockState(pos).getBlock() instanceof ScarecrowBlock) {
                ServerWorld serverWorld = (ServerWorld) worldIn;
                // Remove from capabilities
                worldIn.getCapability(ScarecrowCapabilities.CHUNK_CAPABILITY).ifPresent(tracker -> {
                    ChunkPos chunkPos = serverWorld.getChunk(pos).getPos();
                    tracker.remove(chunkPos, pos);
                });
            }
        }
    }
}
