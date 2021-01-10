package mc.scarecrow.common.capability.pojo;

import java.util.UUID;

public class ScarecrowTilePojo {

    private int currentBurningTime;
    private int totalBurnTime;
    private int totalItemBurnTime;
    private boolean inPause;
    private UUID uuid;
    private UUID closestPlayer;

    public int getCurrentBurningTime() {
        return currentBurningTime;
    }

    public void setCurrentBurningTime(int currentBurningTime) {
        this.currentBurningTime = currentBurningTime;
    }

    public boolean isInPause() {
        return inPause;
    }

    public void setInPause(boolean inPause) {
        this.inPause = inPause;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getTotalBurnTime() {
        return totalBurnTime;
    }

    public void setTotalBurnTime(int totalBurnTime) {
        this.totalBurnTime = totalBurnTime;
    }

    public void setTotalItemBurnTime(int totalItemBurnTime) {
        this.totalItemBurnTime = totalItemBurnTime;
    }

    public int getTotalItemBurnTime() {
        return this.totalItemBurnTime;
    }

    public UUID getClosestPlayer() {
        return closestPlayer;
    }

    public void setClosestPlayer(UUID closestPlayer) {
        this.closestPlayer = closestPlayer;
    }
}
