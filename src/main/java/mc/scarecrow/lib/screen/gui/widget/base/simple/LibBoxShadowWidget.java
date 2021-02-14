package mc.scarecrow.lib.screen.gui.widget.base.simple;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVectorBox;

import static mc.scarecrow.lib.utils.UIUtils.drawBox;

public class LibBoxShadowWidget extends LibSimpleWidgetBase {

    private boolean hovering;

    public LibBoxShadowWidget(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        super(dimensions, z, red, green, blue, alpha);
    }

    public LibBoxShadowWidget(LibVectorBox dimensions, int z) {
        this(dimensions, z, 0, 0, 0, 0.3F);
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
}
