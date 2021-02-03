package mc.scarecrow.lib.screen.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.builder.screen.TextBuffer;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.FixedWidthFontRenderer;
import net.minecraft.util.SharedConstants;

import java.util.concurrent.atomic.AtomicBoolean;

import static mc.scarecrow.lib.screen.gui.FixedWidthFontRenderer.OUT_FONT_HEIGHT;

public class LibWidgetTextInput implements ILibWidget {

    private LibVectorBox dimensions;

    private String text;
    private int z;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private boolean hovering;
    private boolean isFocus;

    private LibWidgetPanel outer;
    private LibBoxShadowWidget boxShadowWidget;
    private LibWidgetAnimator<Void> animator;
    private AtomicBoolean displayCursor;
    private StringBuilder textBuffer;

    public LibWidgetTextInput(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        this.dimensions = dimensions;
        this.text = "";
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.animator = new LibWidgetAnimator<>(this::onAnimate, () -> null, 300L, true);
        this.displayCursor = new AtomicBoolean(false);
        this.textBuffer = new StringBuilder();
    }

    void onAnimate(Void v) {
        this.displayCursor.set(!this.displayCursor.get());
    }

    @Override
    public void init() {
        this.outer = new LibWidgetPanel(dimensions, this.z + 1, red, green, blue, alpha);
        this.outer.init();
        this.boxShadowWidget = new LibBoxShadowWidget(this.z + 1, this.dimensions);
        this.boxShadowWidget.init();
        this.animator.enable();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.outer.render(matrixStack, mouseX, mouseY, partialTicks);
        this.boxShadowWidget.render(matrixStack, mouseX, mouseY, partialTicks);

        TextBuffer textBuffer;
        if (this.displayCursor.get())
            textBuffer = new TextBuffer(this.textBuffer.toString() + "_");
        else
            textBuffer = new TextBuffer(this.textBuffer.toString());


        FixedWidthFontRenderer.drawString(
                dimensions.getLeftTop().getX(),
                dimensions.getLeftTop().getY() + ((this.dimensions.getHeight() - OUT_FONT_HEIGHT) / 2),
                this.z + 1,
                textBuffer);
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return this.dimensions;
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = vectorBox;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public int getZ() {
        return 0;
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        this.hovering = true;
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (isFocus && SharedConstants.isAllowedCharacter(codePoint)) {
            this.textBuffer.append(Character.toString(codePoint));
        }
    }

    @Override
    public void onHoverOut() {
        this.hovering = false;
    }

    @Override
    public void onClick(LibVector2D vector2D, int button) {
        this.isFocus = true;
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onTokenReceived(FocusToken token) {
        token.claimOrReleaseIfNeeded(this, this.dimensions);
    }

    @Override
    public void onFocusChange() {
        this.isFocus = false;
    }

    @Override
    public void onFocusClaimed() {
        this.isFocus = true;
    }
}
