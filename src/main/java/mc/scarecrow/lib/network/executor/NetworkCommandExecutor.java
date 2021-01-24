package mc.scarecrow.lib.network.executor;

import mc.scarecrow.lib.core.libinitializer.ILibElement;
import mc.scarecrow.lib.core.libinitializer.LibElement;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.network.channel.NetworkChannel;
import mc.scarecrow.lib.network.commandhandler.INetworkCommandHandler;
import mc.scarecrow.lib.network.commandhandler.NetworkCommandHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;


@LibElement(after = {NetworkChannel.class, NetworkExecutorService.class})
public final class NetworkCommandExecutor implements NetworkCommandExecutorListener, ILibElement {

    private static NetworkCommandExecutor INSTANCE;

    @LibInject
    private NetworkChannel networkChannel;

    @LibInject
    private NetworkExecutorService executorService;

    private static final Map<String, BiConsumer<ServerPlayerEntity, NetworkCommand>> subscriptions = new TreeMap<>(String::compareTo);
    private int aiId;

    public NetworkCommandExecutor() {
        INSTANCE = this;
    }

    public <T> void addMessage(INetworkCommandHandler<T> handler, Class<T> message) {
        networkChannel.getChannel().messageBuilder(message, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(handler::encodeMessage)
                .decoder(handler::decodeMessage)
                .consumer(handler::consumeMessage)
                .add();
    }

    private int nextId() {
        return aiId++;
    }

    public void sendToServer(NetworkCommand command) {
        this.executorService.executeCommand(command);
    }

    public static void subscribe(String id, BiConsumer<ServerPlayerEntity, NetworkCommand> consumer) {
        if (subscriptions.containsKey(id))
            throw new RuntimeException("Attempts to subscribe with a duplicated id: "
                    + " in: " + NetworkCommandExecutor.class.getSimpleName());

        subscriptions.put(id, consumer);
    }

    @Override
    public void onConsume(ServerPlayerEntity serverPlayerEntity, NetworkCommand networkCommand) {
        BiConsumer<ServerPlayerEntity, NetworkCommand> biConsumer = subscriptions.get(networkCommand.getId());

        if (biConsumer != null)
            biConsumer.accept(serverPlayerEntity, networkCommand);
    }

    @Override
    public void postConstruct(String modId, FMLJavaModLoadingContext loadingContext) {
        INetworkCommandHandler<NetworkCommand> INetworkCommandHandler
                = new NetworkCommandHandler(this);

        addMessage(INetworkCommandHandler, NetworkCommand.class);
    }

    public static NetworkCommandExecutor getInstance() {
        return INSTANCE;
    }
}
