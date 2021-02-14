package mc.scarecrow.lib.utils;

import mc.scarecrow.lib.proxy.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public abstract class TaskUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    private TaskUtils() {
    }

    public static void addScheduledTaskOnSide(IWorld world, Runnable runnable) {
        if (world == null)
            return;

        if (world.isRemote())
            Minecraft.getInstance().execute(runnable);
        else
            ServerLifecycleHooks.getCurrentServer().execute(runnable);
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends TileEntity> void executeIfTileOnClient(World world, BlockPos pos, Class<T> type, Consumer<T> consumer) {
        try {
            if (world != null && world.isRemote() && world instanceof ClientWorld)
                executeIfTile(world, pos, type, consumer);
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
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
            if (world != null && pos != null && type != null) {
                T tile = getTileEntity(world, pos, type);
                if (tile != null)
                    consumer.accept(tile);
            }
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

    public static <T extends TileEntity> void executeIfSided(boolean onServer, Runnable runnable) {
        if (Proxy.PROXY.getSide().isServerLogic() && onServer)
            runnable.run();
        else if (!Proxy.PROXY.getSide().isServerLogic() && !onServer)
            runnable.run();
    }
}
