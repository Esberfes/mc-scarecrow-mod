package mc.scarecrow.lib.proxy;

import mc.scarecrow.lib.enums.SideEnum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.loading.FMLLoader;

public interface IProxy {

    World getPlayerWorld();

    PlayerEntity getPlayerEntity();

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
        if (FMLLoader.getDist().isClient() && isServer())
            return SideEnum.serverOnClient;

        if (FMLLoader.getDist().isClient())
            return SideEnum.client;

        return SideEnum.dedicated;
    }
}