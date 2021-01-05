package mc.scarecrow.common.network;

import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.client.screens.ScarecrowScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        ScreenManager.registerFactory(ScarecrowMod.TYPE, ScarecrowScreen::new);

    }

    @Override
    public Minecraft getClient() {
        return Minecraft.getInstance();
    }

    @Override
    public World getPlayerWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public ClientPlayerEntity getPlayerEntity() {
        return Minecraft.getInstance().player;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return getPlayerEntity().connection.getNetworkManager();
    }

    public <V> CompletableFuture<V> addScheduledTaskClient(Supplier<V> supplier) {
        return Minecraft.getInstance().supplyAsync(supplier);
    }

    @Override
    public CompletableFuture<Void> addScheduledTaskClient(Runnable runnableToSchedule) {
        return Minecraft.getInstance().deferTask(runnableToSchedule);
    }
}