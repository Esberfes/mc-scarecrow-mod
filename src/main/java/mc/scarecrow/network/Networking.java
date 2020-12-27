package mc.scarecrow.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {

    private static SimpleChannel INSTANCE;
    private static int ID = 0;

    public static void registerMessages() {
        /*
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_IDENTIFIER, CHANNEL_PATH),
                () -> "1.0",
                s -> true,
                s -> true);

        INSTANCE.messageBuilder(PacketOpenGui.class, nextID())
                .encoder((packetOpenGui, packetBuffer) -> {
                })
                .decoder(buf -> new PacketOpenGui())
                .consumer(PacketOpenGui::handle)
                .add();
        INSTANCE.messageBuilder(PacketSpawn.class, nextID())
                .encoder(PacketSpawn::toBytes)
                .decoder(PacketSpawn::new)
                .consumer(PacketSpawn::handle)
                .add();
                */

    }

    public static void sendToClient(Object packet, ServerPlayerEntity player) {
        INSTANCE.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }


    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    private static int nextID() {
        return ID++;
    }
}
