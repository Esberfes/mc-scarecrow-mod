package mc.scarecrow.common.block.tile;

import mc.scarecrow.common.block.container.ScarecrowContainer;
import mc.scarecrow.common.capability.pojo.ScarecrowTilePojo;
import mc.scarecrow.common.entity.ScarecrowPlayerEntity;
import mc.scarecrow.common.init.CommonRegistryHandler;
import mc.scarecrow.lib.tile.SyncLockableLootTileBase;
import mc.scarecrow.lib.utils.LogUtils;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static mc.scarecrow.constant.ScarecrowModConstants.INVENTORY_SIZE;
import static mc.scarecrow.lib.utils.UIUtils.ticksToTime;

public class ScarecrowTile extends SyncLockableLootTileBase<ScarecrowTilePojo> implements IComponentProvider {

    private final ScarecrowTileFuelManger fuelManager;
    private final AtomicBoolean taskInProgress;
    private ScarecrowPlayerEntity fakePlayer;
    private UUID owner;
    private double lastYaw = 0D;

    public ScarecrowTile() {
        super(CommonRegistryHandler.scarecrowTileBlock.get(), INVENTORY_SIZE);

        fuelManager = new ScarecrowTileFuelManger(this::getItems);
        taskInProgress = new AtomicBoolean();
        taskInProgress.set(false);
    }

    @Override
    public void onTickServer(ServerWorld world, long serverTicks) {
        if (taskInProgress.get())
            return;

        taskInProgress.set(true);

        world.getServer().deferTask(() -> {
            try {
                if (serverTicks % 10 == 0) {
                    fuelManager.onUpdate();

                    if (isActive() && fakePlayer == null)
                        fakePlayer = ScarecrowPlayerEntity.create(world, getPos(), UUID.randomUUID());

                    if (!isActive() && fakePlayer != null) {
                        ScarecrowPlayerEntity.remove(fakePlayer, world);
                        fakePlayer = null;
                    }

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

    @Override
    public void onTickClient(ClientWorld world, long clientTicks) {

    }

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

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ScarecrowPlayerEntity getFakePlayer() {
        return fakePlayer;
    }

    public double getLastYaw() {
        return lastYaw;
    }

    public void setLastYaw(double lastYaw) {
        this.lastYaw = lastYaw;
    }

    @Override
    protected Class<ScarecrowTilePojo> getPojoClass() {
        return ScarecrowTilePojo.class;
    }

    @Override
    public void onPojoReceived(ScarecrowTilePojo pojo) {
        this.fuelManager.setCurrentBurningTime(pojo.getCurrentBurningTime());
        this.fuelManager.setInPause(pojo.isInPause());
        this.fuelManager.setTotalBurnTime(pojo.getTotalBurnTime());
        this.fuelManager.setTotalItemBurnTime(pojo.getTotalItemBurnTime());
        this.owner = pojo.getUuid();
    }

    @Override
    public ScarecrowTilePojo onPojoRequested() {
        ScarecrowTilePojo result = new ScarecrowTilePojo();

        result.setCurrentBurningTime(this.fuelManager.getCurrentBurningTime());
        result.setTotalBurnTime(this.fuelManager.getTotalBurnTime());
        result.setTotalItemBurnTime(this.fuelManager.getTotalItemBurnTime());
        result.setInPause(this.fuelManager.isInPause());
        result.setUuid(owner);

        return result;
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
}
