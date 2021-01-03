package mc.scarecrow.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

public class ScarecrowBlock extends Block {

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
        // return ScarecrowTileFactory.getInstance(ScarecrowMod.PROXY);
        return new ScarecrowTile();
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

    protected Stat<ResourceLocation> getOpenStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    @Override
    public StateContainer<Block, BlockState> getStateContainer() {
        return super.getStateContainer();
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider) tileentity : null;
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
        super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof ScarecrowTile && !worldIn.isRemote && placer instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerEntity = (ServerPlayerEntity) placer;
            ScarecrowTile scarecrowTile = (ScarecrowTile) tileentity;
            scarecrowTile.loadAll(playerEntity);
        }
    }

    @Override
    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        super.onLanded(worldIn, entityIn);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ScarecrowTile && !worldIn.isRemote) {
                InventoryHelper.dropInventoryItems(worldIn, pos, (ScarecrowTile) tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
                ((ScarecrowTile) tileentity).unloadAll();
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return Container.calcRedstoneFromInventory((IInventory) worldIn.getTileEntity(pos));
    }
}
