package mc.scarecrow.lib.network.executor;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface NetworkCommandExecutorListener {
    void onConsume(ServerPlayerEntity serverPlayerEntity, NetworkCommand networkCommand);
}
