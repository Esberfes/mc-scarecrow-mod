package mc.scarecrow.entity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;

public class FakeServerPlayNetHandler extends ServerPlayNetHandler {

    public FakeServerPlayNetHandler(MinecraftServer server, ServerPlayerEntity playerIn) {
        super(server, new NetworkManager(PacketDirection.SERVERBOUND), playerIn);
    }
}
