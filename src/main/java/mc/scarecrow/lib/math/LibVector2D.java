package mc.scarecrow.lib.math;

import java.util.Objects;

public class LibVector2D implements Comparable<LibVector2D> {

    private final int x;
    private final int y;
    private final float xd;
    private final float yd;

    public LibVector2D() {
        this.x = 0;
        this.y = 0;
        this.xd = 0;
        this.yd = 0;
    }

    public LibVector2D(int x, int y) {
        this.x = x;
        this.y = y;
        this.xd = x;
        this.yd = y;
    }

    public LibVector2D(float x, float y) {
        this.x = (int) x;
        this.y = (int) y;
        this.xd = x;
        this.yd = y;
    }

    public LibVector2D(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
        this.xd = (float) x;
        this.yd = (float) y;
    }

    public LibVector2D(LibVector2D libVector2D) {
        this.x = libVector2D.x;
        this.y = libVector2D.y;
        this.xd = libVector2D.x;
        this.yd = libVector2D.y;
    }

    public LibVector2D clone() {
        return new LibVector2D(x, y);
    }

    public LibVector2D addX(float x) {
        return new LibVector2D(this.x + x, this.y);
    }

    public LibVector2D addY(float y) {
        return new LibVector2D(this.x, this.y + y);
    }

    public LibVector2D add(float x, float y) {
        return new LibVector2D(this.x + x, this.y + y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getXd() {
        return xd;
    }

    public float getYd() {
        return yd;
    }

    public float distance(LibVector2D other) {
        return (float) Math.sqrt(Math.pow((other.getX() - this.getX()), 2) + Math.pow(other.getY() - this.getY(), 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibVector2D that = (LibVector2D) o;
        return x == that.x &&
                y == that.y &&
                Float.compare(that.xd, xd) == 0 &&
                Float.compare(that.yd, yd) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(LibVector2D o) {
        if (x > o.x || (x == o.x && y > o.y))
            return 1;

        if (x < o.x || y < o.y)
            return -1;

        return 0;
    }

    @Override
    public String toString() {
        String sb = "LibVector2D{" + "x=" + x +
                ", y=" + y +
                ", xd=" + xd +
                ", yd=" + yd +
                '}';
        return sb;
    }

    public static LibVector2D relativeTo(LibVector2D other, int xDistance, int yDistance) {
        return new LibVector2D(other.getX() + xDistance, other.getY() + yDistance);
    }

    public static LibVector2D getTopRightCorner(int xBounds, int padding) {
        return new LibVector2D(xBounds - padding, padding);
    }

}
