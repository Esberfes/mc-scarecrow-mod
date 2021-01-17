package mc.scarecrow.lib.network.commandhandler;

import com.google.gson.*;
import mc.scarecrow.lib.network.executor.NetworkCommand;
import mc.scarecrow.lib.network.executor.NetworkCommandExecutorListener;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public class NetworkCommandHandler implements INetworkCommandHandler<NetworkCommand> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Gson gson;
    private final NetworkCommandExecutorListener networkCommandExecutorListener;

    public NetworkCommandHandler(NetworkCommandExecutorListener networkCommandExecutorListener) {
        this.networkCommandExecutorListener = networkCommandExecutorListener;
        this.gson = new GsonBuilder().registerTypeAdapter(Double.class, new DoubleSerializer()).setPrettyPrinting().create();
    }

    private static class DoubleSerializer implements JsonSerializer<Double> {
        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return src == src.longValue() ? new JsonPrimitive(src.longValue()) : new JsonPrimitive(src);
        }
    }

    @Override
    public void consumeMessage(NetworkCommand networkCommand, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        try {
            if (context != null && context.getSender() != null)
                networkCommandExecutorListener.onConsume(context.getSender(), networkCommand);

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
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
            LogUtils.printError(LOGGER, e);
            return null;
        }
    }

    @Override
    public void encodeMessage(NetworkCommand networkCommand, PacketBuffer packetBuffer) {
        try {
            packetBuffer.writeString(gson.toJson(networkCommand));
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }
}
