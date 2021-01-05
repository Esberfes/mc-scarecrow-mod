package mc.scarecrow.common.network;

import mc.scarecrow.common.network.packet.ScarecrowTogglePacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static mc.scarecrow.constant.ScarecrowModConstants.CHANNEL_PATH;
import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

public class Networking {

    public static SimpleChannel INSTANCE;
    private static int ID = 0;

    public static void registerMessages() {

        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_IDENTIFIER, CHANNEL_PATH),
                () -> "1.0",
                s -> true,
                s -> true);

        INSTANCE.messageBuilder(ScarecrowTogglePacket.class, nextID())
                .encoder(ScarecrowTogglePacket::encode)
                .decoder(ScarecrowTogglePacket::decode)
                .consumer(ScarecrowTogglePacket::handle)
                .add();
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
