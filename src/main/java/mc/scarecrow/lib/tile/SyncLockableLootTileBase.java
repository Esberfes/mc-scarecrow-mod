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
import java.util.concurrent.atomic.AtomicLong;

public abstract class SyncLockableLootTileBase<PojoType> extends LockableLootTileEntity implements TilePojoHandler<PojoType>, TileSidedTickHandler {

    protected final Logger LOGGER = LogManager.getLogger();
    private static final String NBT_TILE_DATA_TAG = "NBT_TILE_DATA_TAG";
    protected final NonNullList<ItemStack> chestContents;
    private final AtomicBoolean dataChange;
    private int numPlayersUsing;
    protected AtomicLong serverTicks;
    protected AtomicLong clientTicks;
    private int chestSize;

    protected SyncLockableLootTileBase(TileEntityType<?> tileType, int chestSize) {
        super(tileType);
        this.dataChange = new AtomicBoolean();
        this.chestSize = chestSize;
        this.chestContents = NonNullList.withSize(chestSize, ItemStack.EMPTY);
        this.clientTicks = new AtomicLong();
        this.serverTicks = new AtomicLong();
    }

    protected SyncLockableLootTileBase(TileEntityType<?> tileType) {
        this(tileType, 0);
    }

    @SuppressWarnings("unchecked")
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
            return new CompoundNBT();
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
    public void tick() {
        // Tick on server side
        TileUtils.executeIfTileOnServer(world, pos, getSubClass(), syncLockableLootTileBase -> onTickServer((ServerWorld) world, serverTicks.getAndIncrement()));
        // Tick on client side
        TileUtils.executeIfTileOnClient(world, pos, getSubClass(), syncLockableLootTileBase -> onTickClient((ClientWorld) world, clientTicks.getAndIncrement()));
    }

    @Override
    public void onTickClient(ClientWorld world, long clientTicks) {
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        try {
            CompoundNBT tag = super.write(compound);
            return writeTag(tag);

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return new CompoundNBT();
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        try {
            super.read(state, nbt);
            readTag(nbt);

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        try {
            return writeTag(super.getUpdateTag());

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return new CompoundNBT();
        }
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        try {
            super.handleUpdateTag(state, tag);
            readTag(tag);
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        try {
            readTag(pkt.getNbtCompound());

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        try {
            if (this.dataChange.get()) {
                CompoundNBT compoundNBT = writeTag(new CompoundNBT());
                this.dataChange.set(false);

                return new SUpdateTileEntityPacket(getPos(), 0, compoundNBT);
            }

            return null;

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return null;
        }
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
