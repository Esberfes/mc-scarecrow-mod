package mc.scarecrow.lib.tile;

import com.google.gson.Gson;
import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.lib.utils.LogUtils;
import mc.scarecrow.lib.utils.TileUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SyncLockableLootTileBase<PojoType> extends LockableLootTileEntity implements TilePojoHandler<PojoType>, TileSidedTickHandler {

    protected final Logger LOGGER = LogManager.getLogger();
    private static final String NBT_TILE_DATA_TAG = "NBT_TILE_DATA_TAG";
    protected final NonNullList<ItemStack> chestContents;
    private final AtomicBoolean dataChange;
    private int numPlayersUsing;
    protected AtomicInteger serverTicks;
    protected AtomicInteger clientTicks;
    private int chestSize;

    protected SyncLockableLootTileBase(TileEntityType<? extends SyncLockableLootTileBase> tileType, int chestSize) {
        super(tileType);
        this.dataChange = new AtomicBoolean();
        this.chestSize = chestSize;
        this.chestContents = NonNullList.withSize(chestSize, ItemStack.EMPTY);
        this.clientTicks = new AtomicInteger();
        this.serverTicks = new AtomicInteger();
    }

    protected SyncLockableLootTileBase(TileEntityType<? extends SyncLockableLootTileBase> tileType) {
        this(tileType, 0);
    }

    @Override
    public void tick() {
        TileUtils.executeIfTileOnServer(world, pos, getSubClass(), syncLockableLootTileBase -> onTickServer((ServerWorld) world, serverTicks.getAndIncrement()));
        TileUtils.executeIfTileOnClient(world, pos, getSubClass(), syncLockableLootTileBase -> onTickClient((ClientWorld) world, clientTicks.getAndIncrement()));

    }

    private <T extends SyncLockableLootTileBase<PojoType>> Class<T> getSubClass() {
        return (Class<T>) this.getClass().asSubclass(this.getClass());
    }

    protected abstract Class<PojoType> getPojoClass();

    protected void shouldUpdate() {
        if (this.world == null || this.world.isRemote)
            return;

        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);

        this.dataChange.set(true);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT tag = super.write(compound);

        return writeTag(tag);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        readTag(nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeTag(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        readTag(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readTag(pkt.getNbtCompound());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        if (this.dataChange.get()) {
            CompoundNBT compoundNBT = writeTag(new CompoundNBT());
            this.dataChange.set(false);

            return new SUpdateTileEntityPacket(getPos(), 0, compoundNBT);
        }

        return null;
    }

    private void readTag(CompoundNBT nbt) {
        try {
            if (nbt.contains(NBT_TILE_DATA_TAG))
                onPojoReceived(new Gson().fromJson(nbt.getString(NBT_TILE_DATA_TAG), getPojoClass()));

            this.chestContents.clear();

            if (!this.checkLootAndRead(nbt))
                ItemStackHelper.loadAllItems(nbt, this.chestContents);

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    private CompoundNBT writeTag(CompoundNBT tag) {
        try {
            CompoundNBT compoundNBT = tag == null ? new CompoundNBT() : tag;

            if (!this.checkLootAndWrite(compoundNBT))
                ItemStackHelper.saveAllItems(compoundNBT, this.chestContents);

            if (!compoundNBT.contains(NBT_TILE_DATA_TAG))
                compoundNBT.putString(NBT_TILE_DATA_TAG, new Gson().toJson(onPojoRequested()));

            return compoundNBT;

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }

        return tag;
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0)
                this.numPlayersUsing = 0;

            ++this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    private void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        if (this.world != null && block instanceof ScarecrowBlock) {
            this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, block);
        }
    }

    @Override
    public int getSizeInventory() {
        return chestSize;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return chestContents;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.chestContents.clear();

        for (int i = 0; i < itemsIn.size(); i++)
            if (i < this.chestSize)
                this.getItems().set(i, itemsIn.get(i));

        shouldUpdate();
    }
}
