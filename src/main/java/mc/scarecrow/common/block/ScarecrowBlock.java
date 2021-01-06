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
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Arrays;

public class ScarecrowBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WITH_TILE = BooleanProperty.create("withtile");
    public static final BooleanProperty TILE_POS = BooleanProperty.create("tilepos");

    private BlockPos tilePos;

    public void setTilePos(BlockPos tilePos) {
        this.tilePos = tilePos;
    }

    public ScarecrowBlock() {
        super(Properties
                .create(Material.IRON, MaterialColor.GRAY)
                .sound(SoundType.STONE)
                .hardnessAndResistance(1.5f, 6)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));

        setDefaultState(getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH)
                .with(WITH_TILE, true)
        );

    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext placement) {
        return getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(WITH_TILE, true);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, WITH_TILE);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return state.get(WITH_TILE) ? new ScarecrowTile() : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(WITH_TILE);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(tilePos);
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
        if (placer instanceof FakePlayer) {

            return;
        }
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof ScarecrowTile && !worldIn.isRemote && placer instanceof ServerPlayerEntity) {

            BlockPos oldPos = new BlockPos(pos);
            BlockState newState = state.with(FACING, Direction.NORTH);
            BlockPos newPos = pos.add(0, 2, 0);
            try {
                ServerPlayerEntity playerEntity = (ServerPlayerEntity) placer;
                ScarecrowTile scarecrowTile = (ScarecrowTile) tileentity;

                BlockPos[] positions = new BlockPos[]{
                        pos.add(0, 1, 0),
                        pos.add(0, 2, 0),
                        pos.add(1, 2, 0),
                        pos.add(-1, 2, 0),
                        pos.add(0, 3, 0)
                };

                if (Arrays.stream(positions).allMatch(worldIn::isAirBlock)) {
                    for (int i = 0; i < positions.length; i++) {
                        if (!worldIn.setBlockState(positions[i], state.with(WITH_TILE, false), 3)) {
                            for (int x = i - 1; x >= 0; x--)
                                worldIn.removeBlock(positions[i], false);

                            worldIn.removeBlock(pos, false);
                            worldIn.removeTileEntity(pos);

                            return;
                        }
                        BlockState blockState = worldIn.getBlockState(positions[i]);
                        ((ScarecrowBlock) blockState.getBlock()).tilePos = pos;
                    }
                } else {
                    worldIn.removeBlock(pos, false);
                    worldIn.removeTileEntity(pos);
                }

                scarecrowTile.loadAll(playerEntity, positions);

            } catch (Throwable e) {
                worldIn.removeBlock(pos, false);
                worldIn.removeBlock(newPos, false);
            }
        }
    }

    @Override
    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        super.onLanded(worldIn, entityIn);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (worldIn.isRemote)
                return;

            TileEntity tileentity = worldIn.getTileEntity(tilePos);
            if (tileentity instanceof ScarecrowTile) {
                InventoryHelper.dropInventoryItems(worldIn, pos, (ScarecrowTile) tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
                ((ScarecrowTile) tileentity).unloadAll();
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state) {
        super.onPlayerDestroy(worldIn, pos, state);
        if (worldIn.isRemote())
            return;
        if (tilePos == null)
            return;

        TileEntity tileentity = worldIn.getTileEntity(tilePos);
        if (!(tileentity instanceof ScarecrowTile))
            return
                    ;
        ((ScarecrowTile) tileentity).onDestroy(worldIn, pos);
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        super.onExplosionDestroy(worldIn, pos, explosionIn);
        if (worldIn.isRemote())
            return;

        if (tilePos == null)
            return;

        TileEntity tileentity = worldIn.getTileEntity(tilePos);
        if (!(tileentity instanceof ScarecrowTile))
            return;

        ((ScarecrowTile) tileentity).onDestroy(worldIn, pos);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (worldIn.isRemote())
            return;

        if (tilePos == null)
            return;

        TileEntity tileentity = worldIn.getTileEntity(tilePos);
        if (!(tileentity instanceof ScarecrowTile))
            return;

        ((ScarecrowTile) tileentity).onDestroy(worldIn, pos);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityWalk(worldIn, pos, entityIn);
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

    public BlockPos getTilePos() {
        return tilePos;
    }
}
