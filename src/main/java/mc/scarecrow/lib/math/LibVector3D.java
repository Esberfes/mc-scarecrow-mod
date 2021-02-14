package mc.scarecrow.lib.math;

import java.util.Objects;

public class LibVector3D implements Comparable<LibVector3D>, Cloneable {

    private final float x;
    private final float y;
    private final float z;
    public static final LibVector3D ZERO = new LibVector3D();

    public LibVector3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public LibVector3D(float xd, float yd, float zd) {
        this.x = xd;
        this.y = yd;
        this.z = zd;
    }

    public LibVector3D(LibVector3D libVector2D) {
        this.x = libVector2D.x;
        this.y = libVector2D.y;
        this.z = libVector2D.z;
    }

    @Override
    public LibVector3D clone() {
        return new LibVector3D(x, y, z);
    }

    public LibVector3D addX(float xd) {
        return new LibVector3D(this.x + xd, this.y, this.z);
    }

    public LibVector3D addY(float yd) {
        return new LibVector3D(this.x, this.y + yd, this.z);
    }

    public LibVector3D addZ(float zd) {
        return new LibVector3D(this.x, this.y, this.z + zd);
    }

    public LibVector3D add(float xd, float yd, float zd) {
        return new LibVector3D(this.x + xd, this.y + yd, this.z + zd);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float distance(LibVector3D other) {
        return (float) Math.sqrt(Math.pow(this.x - other.x, 2f)
                + Math.pow(this.y - other.y, 2f)
                + Math.pow(this.z - other.z, 2f));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibVector3D that = (LibVector3D) o;
        return Float.compare(that.x, x) == 0
                && Float.compare(that.y, y) == 0
                && Float.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    // TODO
    @Override
    public int compareTo(LibVector3D o) {
        if (x == o.x && y == o.y && z == o.z)
            return 0;

        return this.distance(ZERO) > o.distance(o) ? 1 : -1;
    }

    @Override
    public String toString() {
        return "LibVector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
