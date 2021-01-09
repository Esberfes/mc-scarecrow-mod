package mc.scarecrow.common.network;

import mc.scarecrow.client.renderer.ScarecrowTileRenderer;
import mc.scarecrow.client.screen.ScarecrowScreen;
import mc.scarecrow.common.init.CommonRegistryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static mc.scarecrow.common.init.events.ContainersRegisterEventHandler.CONTAINER_TYPE;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        // Register on client the screen for the container
        ScreenManager.registerFactory(CONTAINER_TYPE, ScarecrowScreen::new);
        // Register on client the scarecrow tile renderer
        ClientRegistry.bindTileEntityRenderer(CommonRegistryHandler.scarecrowTileBlock.get(),
                o -> new ScarecrowTileRenderer(o, CommonRegistryHandler.scarecrowBlock.get()));
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