package mc.scarecrow.common.block.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ScarecrowTileFuelManger {

    /**
     * Minecraft tick = 0.05 seconds or 50 millis
     */
    private final static double TICK_TIME_RELATION = 50D;

    private final Supplier<NonNullList<ItemStack>> supplier;

    private final AtomicInteger totalBurnTime;
    private final AtomicInteger currentBurningTime;

    private long lastUpdate;
    private final AtomicBoolean inPause;

    public ScarecrowTileFuelManger(Supplier<NonNullList<ItemStack>> supplier) {
        this.supplier = supplier;
        this.totalBurnTime = new AtomicInteger();
        this.currentBurningTime = new AtomicInteger();
        this.inPause = new AtomicBoolean();
        this.inPause.set(false);
    }

    public synchronized void onUpdate() {
        long now = System.currentTimeMillis();
        long timeSinceLastUpdate = now - lastUpdate;
        long ticks = (long) (timeSinceLastUpdate / TICK_TIME_RELATION);
        NonNullList<ItemStack> inventory = supplier.get();

        // if no fuel burning will try to refuel from inventory if not paused
        if (currentBurningTime.get() <= 0 && inventory.size() > 0 && !this.inPause.get()) {
            ItemStack itemStack = inventory.stream().findFirst().orElse(null);
            if (itemStack != null) {
                int count = itemStack.getCount();
                if (count > 0) {
                    ItemStack refuel = itemStack.split(1);
                    currentBurningTime.set(ForgeHooks.getBurnTime(refuel));
                }
            }
        }

        totalBurnTime.set(currentBurningTime.get());

        for (ItemStack itemStack : inventory)
            totalBurnTime.addAndGet(ForgeHooks.getBurnTime(itemStack) * itemStack.getCount());

        if (currentBurningTime.get() > 0) {
            if (!this.inPause.get())
                currentBurningTime.addAndGet((int) -ticks);

            currentBurningTime.set(Math.max(currentBurningTime.get(), 0));
        }

        lastUpdate = now;
    }

    public synchronized boolean active() {
        return !this.inPause.get() && totalBurnTime.get() > 0;
    }

    public synchronized int getTotalBurnTime() {
        return totalBurnTime.get();
    }

    public synchronized int getCurrentBurningTime() {
        return currentBurningTime.get();
    }

    public synchronized boolean isInPause() {
        return inPause.get();
    }

    public synchronized void setInPause(boolean inPause) {
        this.inPause.set(inPause);
    }

    public synchronized boolean toggle() {
        return !this.inPause.getAndSet(!this.inPause.get());
    }

    public synchronized void setCurrentBurningTime(int currentBurningTime) {
        this.currentBurningTime.set(currentBurningTime);
    }
}
