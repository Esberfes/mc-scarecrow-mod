package mc.scarecrow.lib.screen.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;

import static mc.scarecrow.lib.utils.UIUtils.drawBox;

public class LibBoxShadowWidget implements ILibWidget {

    private int z;
    private LibVectorBox dimensions;
    private boolean hovering;

    public LibBoxShadowWidget(int z, LibVectorBox dimensions) {
        this.z = z;
        this.dimensions = dimensions;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawBox(dimensions.relative().withSizeToTop(1F)
                        .move(1F, 1F),
                this.z, 0, 0, 0, 0.3F);
        drawBox(dimensions.relative().withSizeToLeft(1F)
                        .withSizeToBottom(this.dimensions.getHeight() - 1F)
                        .move(1F, 1F),
                this.z, 0, 0, 0, 0.3F);
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return this.dimensions;
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {

    }

    @Override
    public void init() {

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
        this.hovering = true;
    }

    @Override
    public void onHoverOut() {
        this.hovering = false;
    }
}
