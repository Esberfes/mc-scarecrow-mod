package mc.scarecrow.common.capability.pojo;

import java.util.UUID;

public class ScarecrowTilePojo {

    private int currentBurningTime;
    private int totalBurnTime;
    private boolean inPause;
    private UUID uuid;

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
}
