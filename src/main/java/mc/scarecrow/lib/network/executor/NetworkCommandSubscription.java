package mc.scarecrow.lib.network.executor;

import mc.scarecrow.lib.core.libinitializer.LibInject;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;
import java.util.function.BiConsumer;

public final class NetworkCommandSubscription {

    private final String id;

    @LibInject
    private static NetworkCommandExecutor commandExecutor;

    private NetworkCommandSubscription(BiConsumer<ServerPlayerEntity, NetworkCommand> consumer) {
        this.id = UUID.randomUUID().toString();
        commandExecutor.subscribe(id, consumer);
    }

    public void execute(NetworkCommand networkCommand) {
        commandExecutor.sendToServer(networkCommand);
    }

    public NetworkCommandBuilder commandBuilder() {
        return new NetworkCommandBuilder(id);
    }

    public static NetworkCommandSubscription build(BiConsumer<ServerPlayerEntity, NetworkCommand> consumer) {
        return new NetworkCommandSubscription(consumer);
    }
}
