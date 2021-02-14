package mc.scarecrow.lib.math;

import java.util.Objects;

public class LibVector2D implements Comparable<LibVector2D>, Cloneable {

    private final float x;
    private final float y;

    public LibVector2D() {
        this.x = 0;
        this.y = 0;
    }

    public LibVector2D(float xd, float yd) {
        this.x = xd;
        this.y = yd;
    }

    public LibVector2D(LibVector2D libVector2D) {
        this.x = libVector2D.x;
        this.y = libVector2D.y;
    }

    @Override
    public LibVector2D clone() {
        return new LibVector2D(x, y);
    }

    public LibVector2D addX(float x) {
        return new LibVector2D(this.x + x, this.y);
    }

    public LibVector2D addY(float yd) {
        return new LibVector2D(this.x, this.y + yd);
    }

    public LibVector2D add(float xd, float yd) {
        return new LibVector2D(this.x + xd, this.y + yd);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float distance(LibVector2D other) {
        return (float) Math.sqrt(Math.pow((other.getX() - this.getX()), 2) + Math.pow(other.getY() - this.getY(), 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibVector2D that = (LibVector2D) o;
        return Float.compare(that.x, x) == 0 &&
                Float.compare(that.y, y) == 0;
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
        final StringBuffer sb = new StringBuffer("LibVector2D{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append('}');
        return sb.toString();
    }

    public static LibVector2D relativeTo(LibVector2D other, int xDistance, int yDistance) {
        return new LibVector2D(other.getX() + xDistance, other.getY() + yDistance);
    }

    public static LibVector2D getTopRightCorner(int xBounds, int padding) {
        return new LibVector2D(xBounds - padding, padding);
    }

}
