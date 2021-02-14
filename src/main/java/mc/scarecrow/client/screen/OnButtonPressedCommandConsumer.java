package mc.scarecrow.client.screen;

import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.lib.network.executor.NetworkCommand;
import mc.scarecrow.lib.network.executor.NetworkCommandSubscription;
import mc.scarecrow.lib.utils.TaskUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

public class OnButtonPressedCommandConsumer implements BiConsumer<ServerPlayerEntity, NetworkCommand> {

    public static final NetworkCommandSubscription networkCommandSubscription
            = NetworkCommandSubscription.build("OnButtonPressedCommandConsumer", new OnButtonPressedCommandConsumer());

    @Override
    public void accept(ServerPlayerEntity serverPlayerEntity, NetworkCommand command) {
        TaskUtils.executeIfTileOnServer(serverPlayerEntity.getServerWorld(),
                BlockPos.fromLong(((Number) command.getPayload().get("pos")).longValue()),
                ScarecrowTile.class, ScarecrowTile::toggle);
    }
}