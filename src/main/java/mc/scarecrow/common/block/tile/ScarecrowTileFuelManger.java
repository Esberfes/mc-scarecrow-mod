package mc.scarecrow.common.block.tile;

import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ScarecrowTileFuelManger {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Supplier<NonNullList<ItemStack>> supplier;

    private final AtomicInteger totalBurnTime;
    private final AtomicInteger currentBurningTime;
    private final AtomicInteger totalItemBurnTime;

    private long lastUpdate;
    private final AtomicBoolean inPause;

    public ScarecrowTileFuelManger(Supplier<NonNullList<ItemStack>> supplier) {
        this.supplier = supplier;
        this.totalBurnTime = new AtomicInteger();
        this.currentBurningTime = new AtomicInteger();
        this.totalItemBurnTime = new AtomicInteger();
        this.inPause = new AtomicBoolean();
        this.inPause.set(false);
    }

    public synchronized void onUpdate(long serverTicks) {
        try {
            long ticksSinceLastUpdate = serverTicks - lastUpdate;
            NonNullList<ItemStack> inventory = supplier.get();

            // if no fuel burning will try to refuel from inventory if not paused
            if (currentBurningTime.get() <= 0 && inventory.size() > 0 && !this.inPause.get()) {
                ItemStack itemStack = inventory.stream().findFirst().orElse(null);
                if (itemStack != null) {
                    int count = itemStack.getCount();
                    if (count > 0) {
                        ItemStack refuel = itemStack.split(1);
                        currentBurningTime.set(ForgeHooks.getBurnTime(refuel));
                        totalItemBurnTime.set(ForgeHooks.getBurnTime(refuel));
                    }
                }
            }

            totalBurnTime.set(currentBurningTime.get());

            for (ItemStack itemStack : inventory)
                totalBurnTime.addAndGet(ForgeHooks.getBurnTime(itemStack) * itemStack.getCount());

            if (currentBurningTime.get() > 0) {
                if (!this.inPause.get())
                    currentBurningTime.addAndGet((int) -ticksSinceLastUpdate);

                currentBurningTime.set(Math.max(currentBurningTime.get(), 0));
            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        } finally {
            lastUpdate = serverTicks;
        }
    }

    public synchronized boolean active() {
        return !this.inPause.get() && totalBurnTime.get() > 0;
    }

    public synchronized int getTotalBurnTime() {
        return totalBurnTime.get();
    }

    public synchronized void setTotalBurnTime(int value) {
        totalBurnTime.set(value);
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

    public synchronized int getTotalItemBurnTime() {
        return this.totalItemBurnTime.get();
    }

    public synchronized void setTotalItemBurnTime(int totalItemBurnTime) {
        this.totalItemBurnTime.set(totalItemBurnTime);
    }
}
