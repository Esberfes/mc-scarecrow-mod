package mc.scarecrow.common.block.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;

public class ScarecrowTileFuelManger {

    /**
     * Minecraft tick = 0.05 seconds or 50 millis
     */
    private final double tickTimeRelation = 50D;

    private NonNullList<ItemStack> inventory;

    private int totalBurnTime;
    private int currentBurningTime;

    private long lastUpdate;
    private boolean inPause;

    public ScarecrowTileFuelManger(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public synchronized void onUpdate() {
        long now = System.currentTimeMillis();
        long timeSinceLastUpdate = now - lastUpdate;
        long ticks = (long) (timeSinceLastUpdate / tickTimeRelation);

        // if no fuel burning will try to refuel from inventory if not paused
        if (currentBurningTime <= 0 && inventory.size() > 0 && !this.inPause) {
            ItemStack itemStack = inventory.stream().findFirst().get();
            int count = itemStack.getCount();
            if (count > 0) {
                ItemStack refuel = itemStack.split(1);
                currentBurningTime = ForgeHooks.getBurnTime(refuel);
            }
        }

        totalBurnTime = currentBurningTime;

        for (ItemStack itemStack : inventory)
            totalBurnTime += ForgeHooks.getBurnTime(itemStack) * itemStack.getCount();

        if (currentBurningTime > 0) {
            if (!this.inPause)
                currentBurningTime -= ticks;

            currentBurningTime = Math.max(currentBurningTime, 0);
        }

        lastUpdate = now;
    }

    public boolean active() {
        return !this.inPause && totalBurnTime > 0;
    }

    public int getTotalBurnTime() {
        return totalBurnTime;
    }

    public int getCurrentBurningTime() {
        return currentBurningTime;
    }

    public boolean isInPause() {
        return inPause;
    }

    public void setInPause(boolean inPause) {
        this.inPause = inPause;
    }

    public boolean toggle() {
        return (this.inPause = !this.inPause);
    }
}
