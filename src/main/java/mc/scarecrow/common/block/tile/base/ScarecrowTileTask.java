package mc.scarecrow.common.block.tile.base;

public abstract class ScarecrowTileTask implements IScarecrowTileTask {
    @Override
    public final void run() {
        try {
            onStart();
            execute();
        } catch (Throwable e) {
            onError(e);
        } finally {
            onFinish();
        }
    }
}