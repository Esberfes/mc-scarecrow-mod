package mc.scarecrow.lib.screen.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;

import static mc.scarecrow.lib.utils.UIUtils.drawBox;

public class LibWidgetArrowIcon implements ILibWidget {

    private int z;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final Direction direction;
    private LibVectorBox dimensions;
    private LibVectorBox arrow;

    public enum Direction {
        LEFT, RIGHT;
    }

    public LibWidgetArrowIcon(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha, Direction direction) {
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.direction = direction;
        this.dimensions = dimensions;
        this.arrow = this.dimensions.relative();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawBox(arrow, this.z + 1, red, green, blue, alpha);
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return dimensions;
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = dimensions;
    }

    @Override
    public void init() {
        arrow = dimensions.relative()
                .moveLeftTop(0, this.dimensions.getHeight() / 2F)
                .moveLeftBottom(0, this.dimensions.getHeight() / -2F);

        if (direction == Direction.RIGHT)
            arrow = arrow.flipY();

        arrow = arrow.centered(this.dimensions);
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public void onClick(LibVector2D vector2D, int button) {
    }

    @Override
    public void onHover(LibVector2D vector2D) {
    }

    @Override
    public void onHoverOut() {
    }
}
