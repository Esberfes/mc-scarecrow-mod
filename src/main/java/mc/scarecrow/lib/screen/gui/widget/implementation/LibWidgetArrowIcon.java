package mc.scarecrow.lib.screen.gui.widget.implementation;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.base.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.base.icon.IconDirection;

import static mc.scarecrow.lib.utils.UIUtils.drawBox;

public class LibWidgetArrowIcon implements ILibWidget {

    private int z;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final IconDirection direction;
    private LibVectorBox dimensions;

    public LibWidgetArrowIcon(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha, IconDirection direction) {
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.direction = direction;
        this.dimensions = dimensions;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawBox(dimensions, this.z + 1, red, green, blue, alpha);
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return dimensions;
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = vectorBox;
    }

    @Override
    public void init() {
        dimensions = dimensions.relative()
                .moveLeftTop(0, this.dimensions.getHeight() / 2F)
                .moveLeftBottom(0, this.dimensions.getHeight() / -2F);

        if (direction == IconDirection.RIGHT)
            dimensions = dimensions.flipY();
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
