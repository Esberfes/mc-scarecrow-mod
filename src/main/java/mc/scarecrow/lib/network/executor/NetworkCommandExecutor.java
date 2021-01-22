package mc.scarecrow.lib.network.executor;

import mc.scarecrow.lib.core.libinitializer.ILibElement;
import mc.scarecrow.lib.core.libinitializer.LibElement;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.core.libinitializer.OnRegisterManuallyListener;
import mc.scarecrow.lib.network.channel.NetworkChannel;
import mc.scarecrow.lib.network.commandhandler.INetworkCommandHandler;
import mc.scarecrow.lib.network.commandhandler.NetworkCommandHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


@LibElement(after = {NetworkChannel.class, NetworkExecutorService.class})
public final class NetworkCommandExecutor implements NetworkCommandExecutorListener, ILibElement {

    @LibInject
    private NetworkChannel networkChannel;

    @LibInject
    private NetworkExecutorService executorService;

    private final Map<String, BiConsumer<ServerPlayerEntity, NetworkCommand>> subscriptions;
    private int aiId;

    public NetworkCommandExecutor() {
        subscriptions = new HashMap<>();
    }

    public <T> void addMessage(INetworkCommandHandler<T> handler, Class<T> message) {
        networkChannel.getChannel().messageBuilder(message, nextId())
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

    public void subscribe(String id, BiConsumer<ServerPlayerEntity, NetworkCommand> consumer) {
        this.subscriptions.put(id, consumer);
    }

    @Override
    public void onConsume(ServerPlayerEntity serverPlayerEntity, NetworkCommand networkCommand) {
        BiConsumer<ServerPlayerEntity, NetworkCommand> biConsumer = subscriptions.get(networkCommand.getId());

        if (biConsumer != null)
            biConsumer.accept(serverPlayerEntity, networkCommand);
    }

    @Override
    public void postConstruct(String modId, OnRegisterManuallyListener onRegisterManuallyListener, FMLJavaModLoadingContext loadingContext) {
        INetworkCommandHandler<NetworkCommand> INetworkCommandHandler
                = new NetworkCommandHandler(this);

        addMessage(INetworkCommandHandler, NetworkCommand.class);
    }
}
