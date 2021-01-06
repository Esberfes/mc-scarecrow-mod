package mc.scarecrow.common.init.events;

import mc.scarecrow.common.block.tile.ScarecrowTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static mc.scarecrow.common.capability.ScarecrowCapabilities.CHUNK_CAPABILITY;
import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldTickEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event) {
        try {
            if (event.phase != TickEvent.Phase.END || !(event.world instanceof ServerWorld))
                return;

            ServerWorld world = (ServerWorld) event.world;
            ServerChunkProvider chunkProvider = world.getChunkProvider();
            int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
            if (tickSpeed > 0) {
                world.getCapability(CHUNK_CAPABILITY).ifPresent(tracker -> {
                    for (Map.Entry<ChunkPos, List<BlockPos>> entry : tracker.getAll().entrySet()) {
                        ChunkPos pos = entry.getKey();
                        if (anyActive(entry.getValue(), world)) {
                            world.forceChunk(pos.x, pos.z, true);
                            if (chunkProvider.chunkManager.getTrackingPlayers(pos, false).count() == 0)
                                world.tickEnvironment(world.getChunk(pos.x, pos.z), tickSpeed);
                        } else {
                            world.forceChunk(pos.x, pos.z, false);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            LOGGER.error(e);
        }
    }

    private static boolean anyActive(List<BlockPos> pos, ServerWorld world) {
        try {
            for (BlockPos p : pos) {
                TileEntity tileEntity = world.getTileEntity(p);
                if (tileEntity instanceof ScarecrowTile && ((ScarecrowTile) tileEntity).isActive())
                    return true;
            }
        } catch (Throwable e) {
            LOGGER.error(e);
            return false;
        }
        return false;
    }
}
