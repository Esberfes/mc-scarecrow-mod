package mc.scarecrow.common.block;

public class ClientScarecrowTile extends ScarecrowBaseTile {

    @Override
    protected boolean isClient() {
        return false;
    }

    @Override
    protected void execute(int ticks) {

    }

    @Override
    protected Runnable execute() {
        return null;
    }
}
