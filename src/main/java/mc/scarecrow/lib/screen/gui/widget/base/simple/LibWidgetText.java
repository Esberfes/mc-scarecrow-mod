package mc.scarecrow.lib.screen.gui.widget.base.simple;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.render.font.LibScaledFontRenderer;
import mc.scarecrow.lib.screen.gui.render.font.LibTextBuffer;

public class LibWidgetText extends LibSimpleWidgetBase {

    private String text;
    private LibVector2D textPosition;

    public LibWidgetText(LibVectorBox dimensions, LibVector2D textPosition, int z, float red, float green, float blue,
                         float alpha, String text) {
        super(dimensions, z, red, green, blue, alpha);
        this.text = text;
        this.textPosition = textPosition;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        LibTextBuffer libTextBuffer = new LibTextBuffer(this.text);
        LibScaledFontRenderer.drawString(textPosition.getX(), textPosition.getY(), z, libTextBuffer);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LibVector2D getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(LibVector2D textPosition) {
        this.textPosition = textPosition;
    }
}
