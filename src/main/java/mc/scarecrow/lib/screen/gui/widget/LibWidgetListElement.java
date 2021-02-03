package mc.scarecrow.lib.screen.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.builder.screen.TextBuffer;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.FixedWidthFontRenderer;

import static mc.scarecrow.lib.screen.gui.FixedWidthFontRenderer.OUT_FONT_HEIGHT;
import static mc.scarecrow.lib.screen.gui.FixedWidthFontRenderer.OUT_FONT_WIDTH;
import static mc.scarecrow.lib.utils.UIUtils.drawBox;

public class LibWidgetListElement implements ILibWidget {

    private static final int PADDING = 3;
    private LibVectorBox dimensions = new LibVectorBox();
    private String numeration;
    private boolean hovering;
    private Runnable onClickAction;
    private String text;
    private int z;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    private boolean visible;
    private LibBoxShadowWidget boxShadowWidget;

    public LibWidgetListElement() {
        numeration = null;
        this.onClickAction = () -> {
        };
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return dimensions;
    }

    @Override
    public void onClick(LibVector2D vector2D, int button) {
        this.onClickAction.run();
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        this.hovering = true;
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onHoverOut() {
        if (this.hovering) {
            this.hovering = false;
        }
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = vectorBox;
        init();
    }

    @Override
    public void init() {
        this.boxShadowWidget = new LibBoxShadowWidget(this.z, this.dimensions);
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        LibVectorBox outerBox = this.dimensions.relative();

        if (this.hovering)
            drawBox(outerBox, this.z, 191, 212, 223, 1);
        else
            drawBox(outerBox, this.z, 144, 164, 174, 1);

        this.boxShadowWidget.render(matrixStack, mouseX, mouseY, partialTicks);

        String text = (numeration != null ? numeration + "-" : "") + getText();
        int maxWidth = dimensions.getWight() - 18;
        int currentTextWidth = 0;
        StringBuilder finalText = new StringBuilder();

        for (char character : text.toCharArray()) {
            if (currentTextWidth + OUT_FONT_WIDTH <= maxWidth) {
                currentTextWidth += OUT_FONT_WIDTH;
                finalText.append(character);
            } else {
                break;
            }
        }

        TextBuffer textBuffer = new TextBuffer(finalText.toString());
        FixedWidthFontRenderer.drawString(
                dimensions.getLeftTop().getX() + 3,
                dimensions.getLeftTop().getY() + ((this.dimensions.getHeight() - OUT_FONT_HEIGHT) / 2),
                this.z + 1,
                textBuffer);
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public LibWidgetListElement setNumeration(String numeration) {
        this.numeration = numeration;

        return this;
    }

    public LibWidgetListElement setOnClickAction(Runnable action) {
        this.onClickAction = action;

        return this;
    }

    public LibWidgetListElement setText(String text) {
        this.text = text;

        return this;
    }

    public String getText() {
        return text;
    }
}