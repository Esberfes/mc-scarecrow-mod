package mc.scarecrow.lib.screen.gui.widget.implementation;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.render.font.LibScaledFontRenderer;
import mc.scarecrow.lib.screen.gui.render.font.LibTextBuffer;
import mc.scarecrow.lib.screen.gui.widget.base.advance.LibAdvancedWidgetBase;
import mc.scarecrow.lib.screen.gui.widget.base.simple.LibBoxShadowWidget;
import mc.scarecrow.lib.screen.gui.widget.event.LibWidgetEventPropagationCanceler;
import mc.scarecrow.lib.screen.gui.widget.event.observer.LibObservable;
import mc.scarecrow.lib.screen.gui.widget.event.observer.LibObserver;
import mc.scarecrow.lib.screen.gui.widget.focus.FocusToken;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SharedConstants;

import java.util.concurrent.atomic.AtomicBoolean;

import static mc.scarecrow.lib.screen.gui.render.font.LibScaledFontRenderer.OUT_FONT_WIDTH;

public class LibWidgetTextInput extends LibAdvancedWidgetBase {

    private boolean isFocus;
    private static final float PADDING = 1F;

    private LibWidgetPanel outer;
    private LibBoxShadowWidget boxShadowWidget;
    private final LibWidgetAnimator<Void> animator;
    private final AtomicBoolean displayCursor;
    private final LibObservable<StringBuilder> textBuffer;
    private final float maxWidth;

    public LibWidgetTextInput(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha,
                              LibObserver<StringBuilder> observer) {
        super(dimensions, z, red, green, blue, alpha);
        this.animator = new LibWidgetAnimator<>(this::onAnimate, () -> null, 300L, true);
        this.displayCursor = new AtomicBoolean(false);
        this.textBuffer = new LibObservable.Builder<StringBuilder>()
                .value(new StringBuilder())
                .observer(observer)
                .build();
        this.maxWidth = dimensions.getWight() - (PADDING * 2);
    }

    void onAnimate(Void v) {
        this.displayCursor.set(!this.displayCursor.get());
    }

    @Override
    protected void onInitStart() {
        this.outer = new LibWidgetPanel(dimensions, this.z + 1, red, green, blue, alpha);
        attachWidget(this.outer);
        this.boxShadowWidget = new LibBoxShadowWidget(this.dimensions, this.z + 1);
        attachWidget(this.boxShadowWidget);
    }

    @Override
    protected void onInitEnd() {
    }

    @Override
    protected void onRenderEnd(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        LibTextBuffer libTextBuffer;
        if (this.displayCursor.get())
            libTextBuffer = new LibTextBuffer(this.textBuffer.get().toString() + "_");
        else
            libTextBuffer = new LibTextBuffer(this.textBuffer.get().toString());

        LibScaledFontRenderer.drawString(
                dimensions.getLeftTop().getX() + 1,
                dimensions.getLeftBottom().getY() - (this.dimensions.getHeight() / 2F),
                this.z + 2,
                libTextBuffer);
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (isFocus && SharedConstants.isAllowedCharacter(codePoint) && (this.textBuffer.get().length() + 2) * OUT_FONT_WIDTH <= this.maxWidth) {
            this.textBuffer.set(s -> s.append(codePoint));
        }
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
        this.animator.disable();
        this.isFocus = false;
    }

    @Override
    public void onFocusClaimed() {
        this.animator.enable();
        this.isFocus = true;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 259:
                if (this.textBuffer.get().length() > 0)
                    this.textBuffer.set(stringBuilder -> stringBuilder.deleteCharAt(stringBuilder.length() - 1));
                break;
            case 257:
                // Enter
                break;
            case 263:
                // left
                break;
            case 262:
                // Right
                break;
            case 256:
                Minecraft.getInstance().player.closeScreen();
                break;

        }
        logger.info("Key code" + keyCode);
    }
}
