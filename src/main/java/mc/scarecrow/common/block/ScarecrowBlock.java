package mc.scarecrow.common.block;

import mc.scarecrow.client.init.ClientRegistryHandler;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.capability.ScarecrowCapabilities;
import mc.scarecrow.common.entity.ScarecrowPlayerEntity;
import mc.scarecrow.lib.register.LibAutoRegister;
import mc.scarecrow.lib.utils.LogUtils;
import mc.scarecrow.lib.utils.TaskUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class ScarecrowBlock extends Block {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ScarecrowBlock() {
        super(Properties
                .create(Material.IRON, MaterialColor.GRAY)
                .sound(SoundType.STONE)
                .notSolid()
                .hardnessAndResistance(1.5f, 6)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));

        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH)
        );
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);

        if (!dropsOriginal.isEmpty())
            return dropsOriginal;

        return Collections.singletonList(new ItemStack(LibAutoRegister.ITEMS.get("scarecrow"), 1));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ScarecrowTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext placement) {
        return getDefaultState()
                .with(FACING, placement.getPlacementHorizontalFacing());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        try {
            TaskUtils.executeIfTileOnServer(worldIn, pos, ScarecrowTile.class,
                    tile -> NetworkHooks.openGui((ServerPlayerEntity) player, tile, pos));

            return ActionResultType.func_233537_a_(worldIn.isRemote());

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return ActionResultType.FAIL;
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        try {
            TaskUtils.executeIfTileOnServer(worldIn, pos, ScarecrowTile.class, scarecrowTile -> {
                worldIn.getCapability(ScarecrowCapabilities.CHUNK_CAPABILITY).ifPresent(tracker -> {
                    ChunkPos chunkPos = worldIn.getChunk(pos).getPos();
                    tracker.add(chunkPos, pos);
                });
                scarecrowTile.setOwner(((ServerPlayerEntity) placer).getGameProfile().getId());
            });
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        return false;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TaskUtils.executeIfTileOnServer(world, pos, ScarecrowTile.class, scarecrowTile -> {
                InventoryHelper.dropInventoryItems(world, pos, scarecrowTile);
                world.updateComparatorOutputLevel(pos, state.getBlock());
                onRemovedFromWorld(world, pos);
                state.getBlock().onReplaced(state, world, pos, newState, isMoving);
            });
        }
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return Container.calcRedstoneFromInventory((IInventory) worldIn.getTileEntity(pos));
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    /**
     * Block has been removed from world so we make sure that all data attached is removed too
     */
    private void onRemovedFromWorld(World worldIn, BlockPos pos) {
        try {
            TaskUtils.executeIfTileOnServer(worldIn, pos, ScarecrowTile.class, scarecrowTile -> {
                // Remove from capabilities
                worldIn.getCapability(ScarecrowCapabilities.CHUNK_CAPABILITY).ifPresent(tracker -> {
                    ChunkPos chunkPos = worldIn.getChunk(pos).getPos();
                    tracker.remove(chunkPos, pos);
                });
                // Remove fake player
                ScarecrowPlayerEntity.remove(scarecrowTile.getFakePlayer(), (ServerWorld) worldIn);
            });
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        super.animateTick(state, world, pos, random);
        TaskUtils.executeIfTileOnClient(world, pos, ScarecrowTile.class, (t) -> {
            if (t.isActive() && random.nextBoolean()) {
                for (int i = 0; i < 2; i++) {
                    double xOrigin = pos.getX() + 0.5;
                    double yOrigin = pos.getY() + (0.6D + (0.9D - 0.6D) * random.nextDouble());
                    double zOrigin = pos.getZ() + 0.5;
                    double xSpeed = (random.nextFloat() - 0.5D) * 0.3D;
                    double ySpeed = Math.abs((random.nextFloat() - 0.5D) * 0.3D);
                    double zSpeed = (random.nextFloat() - 0.5D) * 0.3D;

                    world.addParticle(ClientRegistryHandler.ClientRegistry.scarecrowParticle, xOrigin, yOrigin, zOrigin, xSpeed, ySpeed, zSpeed);
                }
            }
        });
    }
}
