package mc.scarecrow.lib.screen.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;

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
    private LibBoxShadowWidget boxShadowWidget;
    private LibWidgetArrowIcon arrowIcon;
    private LibWidgetArrowIcon.Direction direction;

    public LibWidgetButton(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha, LibWidgetArrowIcon.Direction direction) {
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
    }

    @Override
    public void init() {
        this.outer = new LibWidgetPanel(dimensions, z, red, green, blue, alpha);
        this.outer.init();
        this.boxShadowWidget = new LibBoxShadowWidget(this.z, this.dimensions);
        this.boxShadowWidget.init();
        this.arrowIcon = new LibWidgetArrowIcon(this.dimensions.relative()
                .withSizeToBottom(this.dimensions.getHeight() - 2)
                .withSizeToRight(this.dimensions.getWight() - 2),
                this.z + 1, 0, 0, 0, 1, direction);
        this.arrowIcon.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.outer.render(matrixStack, mouseX, mouseY, partialTicks);
        this.boxShadowWidget.render(matrixStack, mouseX, mouseY, partialTicks);
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

    @Override
    public Priority getPriority() {
        return Priority.max;
    }

    public boolean getPressing() {
        return pressing.get();
    }
}
