package mc.scarecrow.lib.builder;

import mc.scarecrow.lib.math.LibVector3D;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.Objects;

public class LayerVector implements Comparable<LayerVector>, Comparator<LayerVector> {

    private LibVector3D position;
    private int item;

    public LayerVector(int x, int y, int z, int itemId) {
        this.position = new LibVector3D(x, y, z);
        this.item = itemId;
    }

    public int getItem() {
        return item;
    }

    public BlockPos toRelativeBlockPos(int rx, int ry, int rz) {
        return new BlockPos(
                rx + this.position.getX(),
                ry + this.position.getY(),
                rz + this.position.getZ());
    }

    public void setItem(int item) {
        this.item = item;
    }

    @Override
    public int compare(LayerVector o1, LayerVector o2) {
        return o1.compareTo(o2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LayerVector that = (LayerVector) o;
        return that.equals(this);
    }

    public LibVector3D getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.position, this.item);
    }

    @Override
    public int compareTo(LayerVector o) {
        if (this.position.getY() > o.position.getY())
            return 1;

        if (this.position.getY() < o.position.getY())
            return -1;

        return position.compareTo(o.position);
    }
}
