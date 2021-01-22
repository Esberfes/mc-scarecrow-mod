package mc.scarecrow.lib.network.commandhandler;

import com.google.gson.Gson;
import mc.scarecrow.lib.core.LibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.network.executor.NetworkCommand;
import mc.scarecrow.lib.network.executor.NetworkCommandExecutorListener;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class NetworkCommandHandler extends LibInstanceHandler implements INetworkCommandHandler<NetworkCommand> {

    @LibInject
    private Logger logger;

    @LibInject
    private Gson gson;

    private final NetworkCommandExecutorListener networkCommandExecutorListener;

    public NetworkCommandHandler(NetworkCommandExecutorListener networkCommandExecutorListener) {
        this.networkCommandExecutorListener = networkCommandExecutorListener;
    }

    @Override
    public void consumeMessage(NetworkCommand networkCommand, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        try {
            if (context != null && context.getSender() != null)
                networkCommandExecutorListener.onConsume(context.getSender(), networkCommand);

        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        } finally {
            if (context != null)
                context.setPacketHandled(true);
        }
    }

    @Override
    public NetworkCommand decodeMessage(PacketBuffer packetBuffer) {
        try {
            return new Gson().fromJson(packetBuffer.readString(), NetworkCommand.class);
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
            return null;
        }
    }

    @Override
    public void encodeMessage(NetworkCommand networkCommand, PacketBuffer packetBuffer) {
        try {
            packetBuffer.writeString(gson.toJson(networkCommand));
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }
}
