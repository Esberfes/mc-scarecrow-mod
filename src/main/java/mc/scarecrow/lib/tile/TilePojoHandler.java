package mc.scarecrow.lib.tile;

public interface TilePojoHandler<T> {

    void onPojoReceived(T data);

    T onPojoRequested();
}
