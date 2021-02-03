package mc.scarecrow.lib.screen.gui.widget;

import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LibWidgetAnimator<T> implements Runnable, Closeable {

    private Consumer<T> setter;
    private Supplier<T> getter;
    private long interval;
    private boolean loop;
    private AtomicBoolean running;
    private ScheduledExecutorService executorService;

    public LibWidgetAnimator(Consumer<T> setter, Supplier<T> getter, long interval, boolean loop) {
        this.setter = setter;
        this.getter = getter;
        this.interval = interval;
        this.loop = loop;
        this.running = new AtomicBoolean(false);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this, 0, interval, TimeUnit.MILLISECONDS);

    }

    @Override
    public void run() {
        if (isRunning()) {
            setter.accept(getter.get());
        }
    }

    public void enable() {
        if (!isRunning()) {
            this.running.set(true);
        }
    }

    public void disable() {
        this.running.set(false);
    }

    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void close() {
        this.executorService.shutdownNow();
    }
}
