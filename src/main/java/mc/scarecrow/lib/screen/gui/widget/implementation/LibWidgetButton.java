package mc.scarecrow.lib.screen.gui.widget.implementation;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.base.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.base.icon.IconDirection;
import mc.scarecrow.lib.screen.gui.widget.base.simple.LibBoxShadowWidget;
import mc.scarecrow.lib.screen.gui.widget.event.LibWidgetEventPropagationCanceler;

import java.util.concurrent.atomic.AtomicBoolean;

public class LibWidgetButton implements ILibWidget {

    private LibVectorBox dimensions;
    private int z;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    private boolean hovering;
    private Runnable onClickAction;

    private AtomicBoolean pressing;

    private LibWidgetPanel outer;
    private LibVectorBox outerDimensions;

    private LibBoxShadowWidget boxShadowWidget;

    private LibWidgetArrowIcon arrowIcon;
    private LibVectorBox arrowIconDimensions;

    private IconDirection direction;
    private LibWidgetAnimator<Void> animator;

    public LibWidgetButton(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha, IconDirection direction) {
        this.dimensions = dimensions;
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.onClickAction = () -> {
        };
        this.pressing = new AtomicBoolean(false);
        this.direction = direction;
        this.animator = new LibWidgetAnimator<>(this::onAnimate, () -> null, 300L, true);
    }

    private void onAnimate(Void unused) {

    }

    @Override
    public void init() {
        this.outerDimensions = dimensions.relative();
        this.outer = new LibWidgetPanel(outerDimensions, z, red, green, blue, alpha);
        this.outer.init();
        this.boxShadowWidget = new LibBoxShadowWidget(this.dimensions, this.z);
        this.boxShadowWidget.init();
        this.arrowIconDimensions = this.dimensions.relative()
                .withSizeToBottom(this.dimensions.getHeight() / 2)
                .withSizeToRight(this.dimensions.getWight() / 1.5F)
                .centered(this.dimensions);

        this.arrowIcon = new LibWidgetArrowIcon(arrowIconDimensions, this.z + 1, 0, 0, 0, 0.5F, direction);
        this.arrowIcon.init();
        this.arrowIconDimensions = this.arrowIcon.getDimensionsBox();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (pressing.get()) {
            this.outer.setDimensionsBox(this.outerDimensions.move(0, 1));
            this.arrowIcon.setDimensionsBox(this.arrowIconDimensions.move(0, 1));
        } else {
            this.boxShadowWidget.render(matrixStack, mouseX, mouseY, partialTicks);
            this.outer.setDimensionsBox(this.outerDimensions);
            this.arrowIcon.setDimensionsBox(this.arrowIconDimensions);
        }
        this.outer.render(matrixStack, mouseX, mouseY, partialTicks);
        this.arrowIcon.render(matrixStack, mouseX, mouseY, partialTicks);
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
    public void onClick(LibVector2D vector2D, int button) {
        this.pressing.set(true);
        this.onClickAction.run();
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        this.hovering = true;
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onClickRelease(LibVector2D vector2D, int button) {
        this.pressing.set(false);
    }

    @Override
    public void onHoverOut() {
        this.hovering = false;
    }

    public void setOnClickAction(Runnable onClickAction) {
        this.onClickAction = onClickAction;
    }

    public boolean getPressing() {
        return pressing.get();
    }
}
