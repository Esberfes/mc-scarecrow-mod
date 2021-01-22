package mc.scarecrow.lib.tile;

import com.google.gson.Gson;
import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.proxy.Proxy;
import mc.scarecrow.lib.utils.LogUtils;
import mc.scarecrow.lib.utils.TaskUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class LibLockableTileBase<PojoType> extends LibTileBase implements TilePojoHandler<PojoType>, TileSidedTickHandler {

    @LibInject
    protected Logger logger;

    private static final String NBT_TILE_DATA_TAG = "NBT_TILE_DATA_TAG";
    protected final NonNullList<ItemStack> chestContents;
    private final AtomicBoolean dataChange;
    private int numPlayersUsing;
    protected AtomicLong serverTicks;
    protected AtomicLong clientTicks;
    private final int chestSize;

    protected LibLockableTileBase(TileEntityType<?> tileType, int chestSize) {
        super(tileType);
        this.dataChange = new AtomicBoolean();
        this.chestSize = chestSize;
        this.chestContents = NonNullList.withSize(chestSize, ItemStack.EMPTY);
        this.clientTicks = new AtomicLong();
        this.serverTicks = new AtomicLong();
    }

    protected LibLockableTileBase(TileEntityType<?> tileType) {
        this(tileType, 0);
    }

    @SuppressWarnings("unchecked")
    private <T extends LibLockableTileBase<PojoType>> Class<T> getSubClass() {
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
            LogUtils.printError(logger, e);
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
            LogUtils.printError(logger, e);
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
        TaskUtils.executeIfTileOnServer(world, pos, getSubClass(), libLockableTileBase
                -> onTickServer((ServerWorld) world, serverTicks.getAndIncrement()));

        // Tick on client side
        TaskUtils.executeIfTileOnClient(world, pos, getSubClass(), libLockableTileBase
                -> onTickClient((ClientWorld) world, Proxy.PROXY.getPlayerEntity(), clientTicks.getAndIncrement()));
    }

    @Override
    public void onTickClient(ClientWorld world, ClientPlayerEntity clientPlayerEntity, long clientTicks) {

    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        try {
            CompoundNBT tag = super.write(compound);
            return writeTag(tag);

        } catch (Throwable e) {
            LogUtils.printError(logger, e);
            return new CompoundNBT();
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        try {
            super.read(state, nbt);
            readTag(nbt);

        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        try {
            return writeTag(super.getUpdateTag());

        } catch (Throwable e) {
            LogUtils.printError(logger, e);
            return new CompoundNBT();
        }
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        try {
            super.handleUpdateTag(state, tag);
            readTag(tag);
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        try {
            readTag(pkt.getNbtCompound());

        } catch (Throwable e) {
            LogUtils.printError(logger, e);
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
            LogUtils.printError(logger, e);
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
