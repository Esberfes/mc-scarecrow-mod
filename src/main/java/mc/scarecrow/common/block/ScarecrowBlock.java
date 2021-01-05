package mc.scarecrow.common.block;

import com.mojang.authlib.GameProfile;
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
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class ScarecrowBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private boolean hasTileEntity;

    public ScarecrowBlock() {
        super(Properties
                .create(Material.IRON, MaterialColor.GRAY)
                .sound(SoundType.STONE)
                .hardnessAndResistance(1.5f, 6)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));

        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
        );

        hasTileEntity = true;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext placement) {
        return getDefaultState().with(FACING, placement.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        // return ScarecrowTileFactory.getInstance(ScarecrowMod.PROXY);
        return hasTileEntity ? new ScarecrowTile() : null;
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
                if (worldIn.setBlockState(newPos, newState, 0)) {
                    Block block = worldIn.getBlockState(newPos).getBlock();
                    if (block == state.getBlock()) {
                        TileEntity newTile = worldIn.getTileEntity(newPos);
                        if (newTile instanceof ScarecrowTile) {

                            ScarecrowTile scarecrowTile1 = (ScarecrowTile) newTile;
                            scarecrowTile1.setWorldAndPos(worldIn, newPos);
                            worldIn.removeBlock(oldPos, false);
                            worldIn.removeTileEntity(oldPos);
                            scarecrowTile1.loadAll(playerEntity);

                            FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) worldIn, new GameProfile(UUID.randomUUID(), UUID.randomUUID().toString()));
                            fakePlayer.setPosition(newPos.getX(), newPos.getY() + 1, newPos.getZ());
                            ItemStack itemStack = new ItemStack(Items.COBBLESTONE, 5);
                            fakePlayer.setHeldItem(Hand.MAIN_HAND, itemStack);

                            if (!ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(fakePlayer, Hand.MAIN_HAND,
                                    new BlockRayTraceResult(new Vector3d(0, 0, 0), Direction.UP, newPos, false))).isSuccessOrConsume())
                                throw new Exception();

                            if (!ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(fakePlayer, Hand.MAIN_HAND,
                                    new BlockRayTraceResult(new Vector3d(0, 0, 0), Direction.DOWN, newPos, false))).isSuccessOrConsume())
                                throw new Exception();
                            if (!ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(fakePlayer, Hand.MAIN_HAND,
                                    new BlockRayTraceResult(new Vector3d(0, 0, 0), Direction.UP, new BlockPos(newPos).add(1, 0, 0), false))).isSuccessOrConsume())
                                throw new Exception();
                            if (!ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(fakePlayer, Hand.MAIN_HAND,
                                    new BlockRayTraceResult(new Vector3d(1, 0, 0), Direction.UP, new BlockPos(newPos).add(-1, 0, 0), false))).isSuccessOrConsume())
                                throw new Exception();
                            if (!ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(fakePlayer, Hand.MAIN_HAND,
                                    new BlockRayTraceResult(new Vector3d(0, 0, 0), Direction.DOWN, new BlockPos(newPos).add(0, -1, 0), false))).isSuccessOrConsume())
                                throw new Exception();

                            ((ServerWorld) worldIn).removePlayer(fakePlayer, false);

                        }
                    }
                } else {
                    worldIn.removeBlock(pos, false);
                    throw new Exception();
                }
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

    public boolean isHasTileEntity() {
        return hasTileEntity;
    }

    public void setHasTileEntity(boolean hasTileEntity) {
        this.hasTileEntity = hasTileEntity;
    }


}
