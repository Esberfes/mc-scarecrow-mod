package mc.scarecrow.common.block.tile.base;

import mc.scarecrow.common.init.RegistryHandler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ScarecrowBaseTile extends TileEntity implements ITickableTileEntity {

    protected final Logger logger;
    private final AtomicInteger counter;
    private final ScarecrowThreadPoolExecutor<ScarecrowTileBlockingQueue<IScarecrowTileTask>> executorService;

    public ScarecrowBaseTile() {
        super(RegistryHandler.scarecrowTileBlock.get());

        executorService = new ScarecrowThreadPoolExecutor<>(new ScarecrowTileBlockingQueue<IScarecrowTileTask>());
        executorService.prestartAllCoreThreads();

        counter = new AtomicInteger();
        logger = LogManager.getLogger();
    }

    @Override
    public final void tick() {
        int ticks = counter.incrementAndGet();
        try {
            onUpdate();

            if (ticks % 10 == 0)
                onUpdate10();

            if (ticks % 100 == 0)
                onUpdate100();


        } catch (Throwable e) {
            logger.error(e);
        } finally {
            if (ticks % 1000 == 0)
                counter.set(0);
        }
    }

    protected void onUpdate() {

    }

    protected void onUpdate10() {

    }

    protected void onUpdate100() {

    }

    protected abstract boolean isClient();

    protected final void enqueueTask(ScarecrowTileTask task) {
        executorService.execute(task);
    }

    private static class ScarecrowTileBlockingQueue<Task extends IScarecrowTileTask> extends LinkedBlockingQueue<Task> {

    }

    @SuppressWarnings("all")
    private static class ScarecrowThreadPoolExecutor<Queue extends ScarecrowTileBlockingQueue> extends ThreadPoolExecutor {

        public ScarecrowThreadPoolExecutor(Queue workQueue) {
            super(10, 10, 0L, TimeUnit.MILLISECONDS, workQueue, new ScarecrowTileThreadFactory());
        }

        private static class ScarecrowTileThreadFactory implements ThreadFactory {

            private int counter;

            public ScarecrowTileThreadFactory() {
                counter = 0;
            }

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "scarecrow-tile-thread-" + counter++);
            }
        }
    }
}
