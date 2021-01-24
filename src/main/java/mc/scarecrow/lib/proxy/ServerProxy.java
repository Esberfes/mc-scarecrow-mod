package mc.scarecrow.lib.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ServerProxy implements IProxy {

    @Override
    public World getPlayerWorld() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public PlayerEntity getPlayerEntity() {
        throw new IllegalStateException("Only run this on the client!");
    }
}