package mc.scarecrow.common.block.tile;

import com.google.gson.Gson;
import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.container.ScarecrowContainer;
import mc.scarecrow.common.capability.pojo.ScarecrowTilePojo;
import mc.scarecrow.common.init.RegistryHandler;
import mc.scarecrow.utils.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static mc.scarecrow.constant.ScarecrowModConstants.INVENTORY_SIZE;

public class ScarecrowTile extends LockableLootTileEntity implements ITickableTileEntity {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NBT_TILE_DATA = "pojo";

    private NonNullList<ItemStack> chestContents;
    private int numPlayersUsing;
    private final ScarecrowTileFuelManger fuelManger;

    private final AtomicInteger serverTickCounter;
    private final AtomicBoolean dataChange;
    private final AtomicBoolean taskInProgress;

    public ScarecrowTile() {
        super(RegistryHandler.scarecrowTileBlock.get());
        numPlayersUsing = 0;
        chestContents = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

        fuelManger = new ScarecrowTileFuelManger(this::getItems);

        serverTickCounter = new AtomicInteger();
        dataChange = new AtomicBoolean();
        taskInProgress = new AtomicBoolean();
        taskInProgress.set(false);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return chestContents;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.chestContents.clear();

        for (int i = 0; i < itemsIn.size(); i++) {
            if (i < this.chestContents.size()) {
                this.getItems().set(i, itemsIn.get(i));
            }
        }

        shouldUpdate();
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new StringTextComponent("Scarecrow");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return null;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player, PlayerEntity entity) {
        try {
            if (world == null)
                throw new Exception("World is null on create menu");

            return new ScarecrowContainer(id, world, getPos(), player, entity);

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return null;
        }
    }

    @Override
    public int getSizeInventory() {
        return chestContents.size();
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote && this.world instanceof ServerWorld)
            runOnServer((ServerWorld) this.world);
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

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
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT tag = super.write(compound);

        return writeTag(tag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeTag(super.getUpdateTag());
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        readTag(nbt);
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
            if (nbt.contains(NBT_TILE_DATA))
                fromPojo(new Gson().fromJson(nbt.getString(NBT_TILE_DATA), ScarecrowTilePojo.class));

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

            if (!this.checkLootAndWrite(tag))
                ItemStackHelper.saveAllItems(tag, this.chestContents);

            compoundNBT.putString(NBT_TILE_DATA, new Gson().toJson(toPojo()));

            return compoundNBT;

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }

        return tag;
    }

    private void runOnServer(ServerWorld serverWorld) {
        int ticks = serverTickCounter.getAndIncrement();

        if (taskInProgress.get())
            return;

        taskInProgress.set(true);

        serverWorld.getServer().deferTask(() -> {
            try {
                if (ticks % 10 == 0)
                    fuelManger.onUpdate();

                if (ticks % 100 == 0) {
                    serverTickCounter.set(0);
                    shouldUpdate();
                }

            } catch (Throwable e) {
                LogUtils.printError(LOGGER, e);
            } finally {
                // Release flag for next update
                taskInProgress.set(false);
            }
        });
    }

    /**
     * Called from WorldTick event when checked to keep chunk enabled
     *
     * @return this tile is active
     */
    public boolean isActive() {
        return this.fuelManger.active();
    }

    public void fromPojo(ScarecrowTilePojo pojo) {
        this.fuelManger.setCurrentBurningTime(pojo.getCurrentBurningTime());
        this.fuelManger.setInPause(pojo.isInPause());
    }

    public ScarecrowTilePojo toPojo() {
        ScarecrowTilePojo result = new ScarecrowTilePojo();

        result.setCurrentBurningTime(this.fuelManger.getCurrentBurningTime());
        result.setInPause(this.fuelManger.isInPause());

        return result;
    }

    public void shouldUpdate() {
        if (this.world == null || this.world.isRemote)
            return;

        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);

        this.dataChange.set(true);
    }
}
