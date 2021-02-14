package mc.scarecrow.lib.network.executor;

import mc.scarecrow.lib.core.LibInstanceHandler;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.function.BiConsumer;

public final class NetworkCommandSubscription extends LibInstanceHandler {

    private final String id;

    private NetworkCommandSubscription(String id, BiConsumer<ServerPlayerEntity, NetworkCommand> consumer) {
        this.id = id;
        NetworkCommandExecutor.subscribe(id, consumer);
    }

    public void execute(NetworkCommand networkCommand) {
        NetworkCommandExecutor.getInstance().sendToServer(networkCommand);
    }

    public NetworkCommandBuilder commandBuilder() {
        return new NetworkCommandBuilder(id);
    }

    public static NetworkCommandSubscription build(String id, BiConsumer<ServerPlayerEntity, NetworkCommand> consumer) {
        return new NetworkCommandSubscription(id, consumer);
    }
}
