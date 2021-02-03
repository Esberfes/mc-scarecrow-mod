package mc.scarecrow.lib.screen.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;

import static mc.scarecrow.lib.utils.UIUtils.drawBox;

public class LibWidgetPanel implements ILibWidget {
    {
        ILibInstanceHandler.fire(this);
    }

    private LibVectorBox dimensions;
    private int z;
    private float red;
    private float green;
    private float blue;
    private float alpha;

    public LibWidgetPanel(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        this.dimensions = dimensions;
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return dimensions;
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

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = vectorBox;
    }

    @Override
    public void init() {

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawBox(dimensions, this.z, this.red, this.green, this.blue, this.alpha);
    }

    public LibVectorBox getDimensions() {
        return dimensions;
    }

    public void setDimensions(LibVectorBox dimensions) {
        this.dimensions = dimensions;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
