package mc.scarecrow.lib.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;

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
    public ClientPlayerEntity getPlayerEntity() {
        throw new IllegalStateException("Only run this on the client!");
    }
}