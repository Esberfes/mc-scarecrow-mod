package mc.scarecrow.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public abstract class TileUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    private TileUtils() {
    }

    public static <T extends TileEntity> T getTileEntity(World world, BlockPos pos, Class<T> type) {
        try {
            T result = null;

            if (world != null && pos != null) {
                TileEntity tileEntity = world.getTileEntity(pos);
                if (type.isInstance(tileEntity)) {
                    result = type.cast(tileEntity);
                }
            }

            return result;

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return null;
        }
    }

    public static <T extends TileEntity> void executeIfTile(World world, BlockPos pos, Class<T> type, Consumer<T> consumer) {
        try {
            T tile = getTileEntity(world, pos, type);
            if (tile != null)
                consumer.accept(tile);
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    public static <T extends TileEntity> void executeIfTileOnServer(World world, BlockPos pos, Class<T> type, Consumer<T> consumer) {
        try {
            if (world != null && !world.isRemote() && world instanceof ServerWorld)
                executeIfTile(world, pos, type, consumer);
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }
}
