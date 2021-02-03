package mc.scarecrow.lib.builder;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class LayerVector implements Comparable<LayerVector> {

    private final int x;
    private final int z;
    private final int itemId;

    public LayerVector(int x, int z, int itemId) {
        this.x = x;
        this.z = z;
        this.itemId = itemId;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getItemId() {
        return itemId;
    }

    public BlockPos toRelativeBlockPos(int rx, int ry, int rz) {
        return new BlockPos(
                rx + x,
                ry,
                rz + z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LayerVector that = (LayerVector) o;
        return x == that.x &&
                z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public int compareTo(LayerVector o) {
        if (x > o.x || (x == o.x && z > o.z))
            return 1;

        if (x < o.x || z < o.z)
            return -1;

        return 0;
    }

    @Override
    public String toString() {
        return "LayerVector{" +
                "x=" + x +
                ", z=" + z +
                ", itemId=" + itemId +
                '}';
    }
}
