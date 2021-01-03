package mc.scarecrow.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface IProxy {
    void init();

    Minecraft getClient();

    World getPlayerWorld();

    PlayerEntity getPlayerEntity();

    NetworkManager getNetworkManager();

    <V> CompletableFuture<V> addScheduledTaskClient(Supplier<V> supplier);

    CompletableFuture<Void> addScheduledTaskClient(Runnable runnableToSchedule);

    default boolean isClient() {
        return this instanceof ClientProxy;
    }

    default boolean isServer() {
        return this instanceof ServerProxy;
    }
}
