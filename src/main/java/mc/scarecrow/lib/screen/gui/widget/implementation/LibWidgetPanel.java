package mc.scarecrow.lib.screen.gui.widget.implementation;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.base.advance.LibAdvancedWidgetBase;

import static mc.scarecrow.lib.utils.UIUtils.drawBox;

public class LibWidgetPanel extends LibAdvancedWidgetBase {

    public LibWidgetPanel(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        super(dimensions, z, red, green, blue, alpha);
    }

    @Override
    protected void onInitStart() {

    }

    @Override
    protected void onInitEnd() {

    }

    @Override
    protected void onRenderStart(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawBox(dimensions, this.z, this.red, this.green, this.blue, this.alpha);
    }
}
