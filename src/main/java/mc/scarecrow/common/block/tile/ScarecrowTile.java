package mc.scarecrow.common.block.tile;

import com.google.gson.Gson;
import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.container.ScarecrowContainer;
import mc.scarecrow.common.capability.pojo.ScarecrowTilePojo;
import mc.scarecrow.common.entity.ScarecrowPlayerEntity;
import mc.scarecrow.common.init.RegistryHandler;
import mc.scarecrow.utils.LogUtils;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static mc.scarecrow.constant.ScarecrowModConstants.INVENTORY_SIZE;
import static mc.scarecrow.utils.UIUtils.ticksToTime;

public class ScarecrowTile extends LockableLootTileEntity implements ITickableTileEntity, IComponentProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NBT_TILE_DATA = "pojo";

    private final NonNullList<ItemStack> chestContents;
    private int numPlayersUsing;
    private final ScarecrowTileFuelManger fuelManager;

    private final AtomicInteger serverTickCounter;
    private final AtomicBoolean dataChange;
    private final AtomicBoolean taskInProgress;

    private ScarecrowPlayerEntity fakePlayer;
    private UUID owner;

    private double lastYaw = 0D;

    public ScarecrowTile() {
        super(RegistryHandler.scarecrowTileBlock.get());
        numPlayersUsing = 0;
        chestContents = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

        fuelManager = new ScarecrowTileFuelManger(this::getItems);

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

        for (int i = 0; i < itemsIn.size(); i++)
            if (i < this.chestContents.size())
                this.getItems().set(i, itemsIn.get(i));

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

    /**
     * All this code will be executed on server and should be async
     */
    private void runOnServer(ServerWorld serverWorld) {
        int ticks = serverTickCounter.getAndIncrement();

        if (taskInProgress.get())
            return;

        taskInProgress.set(true);

        serverWorld.getServer().deferTask(() -> {
            try {
                if (ticks % 10 == 0) {
                    fuelManager.onUpdate();

                    if (isActive() && fakePlayer == null)
                        fakePlayer = ScarecrowPlayerEntity.create(serverWorld, getPos(), UUID.randomUUID());

                    if (!isActive() && fakePlayer != null) {
                        ScarecrowPlayerEntity.remove(fakePlayer, (ServerWorld) world);
                        fakePlayer = null;
                    }

                    shouldUpdate();
                }
            } catch (Throwable e) {
                LogUtils.printError(LOGGER, e);
            } finally {
                // Make sure counter not overflow
                if (ticks % 1000 == 0)
                    serverTickCounter.set(1);

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
        return this.fuelManager.active();
    }

    public boolean isPaused() {
        return this.fuelManager.isInPause();
    }

    public int getTotalFuel() {
        return this.fuelManager.getTotalBurnTime();
    }

    public int getTotalItemBurnTime() {
        return this.fuelManager.getTotalItemBurnTime();
    }

    public int getCurrentBurnTime() {
        return this.fuelManager.getCurrentBurningTime();
    }

    /**
     * Deserialize data to sync
     */
    public void fromPojo(ScarecrowTilePojo pojo) {
        this.fuelManager.setCurrentBurningTime(pojo.getCurrentBurningTime());
        this.fuelManager.setInPause(pojo.isInPause());
        this.fuelManager.setTotalBurnTime(pojo.getTotalBurnTime());
        this.fuelManager.setTotalItemBurnTime(pojo.getTotalItemBurnTime());
        this.owner = pojo.getUuid();
    }

    /**
     * Serialize data to sync
     */
    public ScarecrowTilePojo toPojo() {
        ScarecrowTilePojo result = new ScarecrowTilePojo();

        result.setCurrentBurningTime(this.fuelManager.getCurrentBurningTime());
        result.setTotalBurnTime(this.fuelManager.getTotalBurnTime());
        result.setTotalItemBurnTime(this.fuelManager.getTotalItemBurnTime());
        result.setInPause(this.fuelManager.isInPause());
        result.setUuid(owner);

        return result;
    }

    /**
     * Update and sync entity
     */
    public void shouldUpdate() {
        if (this.world == null || this.world.isRemote)
            return;

        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);

        this.dataChange.set(true);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ScarecrowPlayerEntity getFakePlayer() {
        return fakePlayer;
    }

    @Override
    public void appendTail(List<ITextComponent> info, IDataAccessor accessor, IPluginConfig config) {
        if (accessor.getTileEntity() instanceof ScarecrowTile) {
            ScarecrowTile tile = (ScarecrowTile) accessor.getTileEntity();
            info.add(new StringTextComponent("Active: " + (tile.isActive() ? "yes" : "no")));
            info.add(new StringTextComponent("Pause: " + (tile.isPaused() ? "yes" : "no")));
            info.add(new StringTextComponent("Fuel: " + ticksToTime(tile.getTotalFuel())));
            ClientPlayerEntity player;
            if (tile.getOwner() != null && accessor.getWorld() != null && (player = (ClientPlayerEntity) accessor.getWorld().getPlayerByUuid(tile.getOwner())) != null)
                info.add(new StringTextComponent("Owner: " + player.getGameProfile().getName()));
        }
    }

    @Override
    public ItemStack getStack(IDataAccessor accessor, IPluginConfig config) {
        return ((ScarecrowTile) accessor.getTileEntity()).chestContents.get(0);
    }

    public double getLastYaw() {
        return lastYaw;
    }

    public void setLastYaw(double lastYaw) {
        this.lastYaw = lastYaw;
    }
}
