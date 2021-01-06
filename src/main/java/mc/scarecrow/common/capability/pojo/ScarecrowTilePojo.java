package mc.scarecrow.common.capability.pojo;

public class ScarecrowTilePojo {

    private int currentBurningTime;
    private boolean inPause;

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
}
