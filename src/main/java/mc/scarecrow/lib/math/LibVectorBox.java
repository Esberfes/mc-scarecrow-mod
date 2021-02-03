package mc.scarecrow.lib.math;

/**
 * Represents a box, a vector for each corner.
 * This object is immutable, if any operation needs change a value new instance will be returned.
 */
public class LibVectorBox {

    private final LibVector2D leftTop;
    private final LibVector2D leftBottom;
    private final LibVector2D rightTop;
    private final LibVector2D rightBottom;
    private final int height;
    private final int wight;

    public LibVectorBox(int x0, int x1, int y0, int y1) {
        this.leftTop = new LibVector2D(x0, y0);
        this.leftBottom = new LibVector2D(x0, y1);
        this.rightTop = new LibVector2D(x1, y0);
        this.rightBottom = new LibVector2D(x1, y1);
        this.wight = Math.abs(x1 - x0);
        this.height = Math.abs(y1 - y0);
    }

    public LibVectorBox(float x0, float x1, float y0, float y1) {
        this.leftTop = new LibVector2D(x0, y0);
        this.leftBottom = new LibVector2D(x0, y1);
        this.rightTop = new LibVector2D(x1, y0);
        this.rightBottom = new LibVector2D(x1, y1);
        this.wight = Math.abs(Math.max(rightTop.getX() - leftTop.getX(), rightBottom.getX() - leftBottom.getX()));
        this.height = Math.abs(Math.max(leftBottom.getY() - leftTop.getY(), rightBottom.getY() - rightTop.getY()));
    }

    public LibVectorBox() {
        this.leftTop = new LibVector2D();
        this.leftBottom = new LibVector2D();
        this.rightTop = new LibVector2D();
        this.rightBottom = new LibVector2D();
        this.wight = 0;
        this.height = 0;
    }

    public LibVectorBox(LibVector2D leftTop, LibVector2D leftBottom, LibVector2D rightTop, LibVector2D rightBottom) {
        this.leftTop = leftTop.clone();
        this.leftBottom = leftBottom.clone();
        this.rightTop = rightTop.clone();
        this.rightBottom = rightBottom.clone();
        this.wight = Math.abs(Math.max(rightTop.getX() - leftTop.getX(), rightBottom.getX() - leftBottom.getX()));
        this.height = Math.abs(Math.max(leftBottom.getY() - leftTop.getY(), rightBottom.getY() - rightTop.getY()));
    }

    public LibVector2D getLeftTop() {
        return new LibVector2D(leftTop);
    }

    public LibVector2D getLeftBottom() {
        return new LibVector2D(leftBottom);
    }

    public LibVector2D getRightTop() {
        return new LibVector2D(rightTop);
    }

    public LibVector2D getRightBottom() {
        return new LibVector2D(rightBottom);
    }

    public boolean isCollisionTo(LibVector2D vector2D) {
        return vector2D.getXd() >= this.getLeftTop().getXd()
                && vector2D.getXd() <= this.getRightTop().getXd()
                && vector2D.getYd() >= this.getLeftTop().getYd()
                && vector2D.getYd() <= this.getLeftBottom().getYd();
    }

    public LibVectorBox relative() {
        return new LibVectorBox(this.leftTop, this.leftBottom, this.rightTop, this.rightBottom);
    }

    public LibVectorBox withSizeToRight(float x) {
        return new LibVectorBox(this.leftTop, this.leftBottom, this.leftTop.addX(x), this.leftBottom.addX(x));
    }

    public LibVectorBox withSizeToLeft(float x) {
        return new LibVectorBox(this.rightTop.addX(-x), this.rightBottom.addX(-x), this.rightTop, this.rightBottom);
    }

    public LibVectorBox withSizeToBottom(float y) {
        return new LibVectorBox(this.leftTop, this.leftTop.addY(y), this.rightTop, this.rightTop.addY(y));
    }

    public LibVectorBox withSizeToTop(float y) {
        return new LibVectorBox(this.leftBottom.addY(-y), this.leftBottom, this.rightBottom.addY(-y), this.rightBottom);
    }

    public LibVectorBox move(float x, float y) {
        return new LibVectorBox(this.leftTop.add(x, y), this.leftBottom.add(x, y), this.rightTop.add(x, y), this.rightBottom.add(x, y));
    }

    public LibVectorBox moveLeftTop(float x, float y) {
        return new LibVectorBox(this.leftTop.add(x, y), this.leftBottom, this.rightTop, this.rightBottom);
    }

    public LibVectorBox moveLeftBottom(float x, float y) {
        return new LibVectorBox(this.leftTop, this.leftBottom.add(x, y), this.rightTop, this.rightBottom);
    }

    public LibVectorBox moveRightTop(float x, float y) {
        return new LibVectorBox(this.leftTop, this.leftBottom, this.rightTop.add(x, y), this.rightBottom);
    }

    public LibVectorBox moveRightBottom(float x, float y) {
        return new LibVectorBox(this.leftTop, this.leftBottom, this.rightTop, this.rightBottom.add(x, y));
    }

    public LibVectorBox flipY() {
        return new LibVectorBox(
                new LibVector2D(this.leftTop.getXd(), this.rightTop.getYd()),
                new LibVector2D(this.leftBottom.getXd(), this.rightBottom.getYd()),
                new LibVector2D(this.rightTop.getXd(), this.leftTop.getYd()),
                new LibVector2D(this.rightBottom.getXd(), this.leftBottom.getYd())
        );
    }

    public LibVectorBox centered(LibVectorBox other) {
        float ownCenterX = (this.rightTop.getXd() - this.leftBottom.getXd()) / 2F;
        float ownCenterY = (this.leftBottom.getYd() - this.rightTop.getXd()) / 2F;
        float otherCenterX = (other.rightTop.getXd() - other.leftBottom.getXd()) / 2F;
        float otherCenterY = (other.leftBottom.getYd() - other.rightTop.getXd()) / 2F;


        return move(otherCenterX > ownCenterX ? otherCenterX - ownCenterX : ownCenterX - otherCenterX,
                otherCenterY > ownCenterY ? otherCenterY - ownCenterY : ownCenterY - otherCenterY);
    }

    public int getHeight() {
        return height;
    }

    public int getWight() {
        return wight;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LibVectorBox{");
        sb.append("leftTop=").append(leftTop);
        sb.append(", leftBottom=").append(leftBottom);
        sb.append(", rightTop=").append(rightTop);
        sb.append(", rightBottom=").append(rightBottom);
        sb.append(", height=").append(height);
        sb.append(", wight=").append(wight);
        sb.append('}');
        return sb.toString();
    }
}
