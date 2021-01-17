package mc.scarecrow.lib.proxy;

import mc.scarecrow.lib.enums.SideEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;

public interface IProxy {

    void init();

    Minecraft getClient();

    World getPlayerWorld();

    ClientPlayerEntity getPlayerEntity();

    default boolean isClient() {
        return this instanceof ClientProxy;
    }

    /**
     * @return true if current thread is running on logical server
     */
    default boolean isServer() {
        return Proxy.isServerLogic();
    }

    /**
     * client when is not a dedicated server and not running on logical server
     * serverOnClient server running inside a client like single player game
     */
    default SideEnum getSide() {
        if (isClient() && isServer())
            return SideEnum.serverOnClient;

        if (isClient())
            return SideEnum.client;

        return SideEnum.dedicated;
    }
}