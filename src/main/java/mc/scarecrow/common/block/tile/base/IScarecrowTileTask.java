package mc.scarecrow.common.block.tile.base;

public interface IScarecrowTileTask extends Runnable {
    void onStart();

    void onFinish();

    void execute();

    void onError(Throwable e);
}
