package mc.scarecrow.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ServerProxy implements IProxy {

    @Override
    public void init() {

    }

    @Override
    public Minecraft getClient() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public World getPlayerWorld() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public PlayerEntity getPlayerEntity() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public NetworkManager getNetworkManager() {
        return null;
    }

    @Override
    public <V> CompletableFuture<V> addScheduledTaskClient(Supplier<V> supplier) {
        throw new IllegalStateException("This should only be called from client side");
    }

    @Override
    public CompletableFuture<Void> addScheduledTaskClient(Runnable runnableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }
}