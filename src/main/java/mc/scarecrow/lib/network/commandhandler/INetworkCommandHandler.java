package mc.scarecrow.lib.network.commandhandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface INetworkCommandHandler<T> {
    void consumeMessage(T networkCommand, Supplier<NetworkEvent.Context> contextSupplier);

    T decodeMessage(PacketBuffer packetBuffer);

    void encodeMessage(T networkCommand, PacketBuffer packetBuffer);

}
