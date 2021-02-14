package mc.scarecrow.lib.screen.gui.widget.base.advance;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.base.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.event.LibWidgetEventPropagationCanceler;
import mc.scarecrow.lib.screen.gui.widget.event.LibWidgetEventPropagator;
import mc.scarecrow.lib.screen.gui.widget.focus.FocusToken;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Lifecyle:
 * <p>
 * 1 - init - when resize screen
 * 2 - render - loop
 * 3 - update - loop after render
 */
public abstract class LibAdvancedWidgetBase implements ILibWidget {
    {
        ILibInstanceHandler.fire(this);
    }

    @LibInject
    protected Logger logger;

    protected LibVectorBox dimensions;
    protected int z;
    protected float red;
    protected float green;
    protected float blue;
    protected float alpha;

    protected AtomicBoolean hovering;
    protected AtomicBoolean pressing;
    protected final LibWidgetEventPropagator widgetEventPropagator;

    protected List<AtomicReference<ILibWidget>> attachedWidgets;
    private final Object lockWidgetAttachment = new Object();

    public LibAdvancedWidgetBase(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        this.dimensions = dimensions;
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.pressing = new AtomicBoolean(false);
        this.hovering = new AtomicBoolean(false);
        this.attachedWidgets = new LinkedList<>();
        this.widgetEventPropagator = new LibWidgetEventPropagator(this.attachedWidgetsSupplier());
    }

    protected void onInitStart() {
    }

    @Override
    public final void init() {
        try {
            this.onInitStart();

            this.attachedWidgetsSupplier()
                    .get()
                    .forEach(ILibWidget::init);

            this.onInitEnd();
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
            Minecraft.getInstance().player.closeScreen();
        }
    }

    protected void onInitEnd() {
    }

    @Override
    public final void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        try {
            // Signal to the implementation, start render, before attached
            onRenderStart(matrixStack, mouseX, mouseY, partialTicks);

            // Render all visible attached widgets
            this.attachedWidgetsSupplier()
                    .get()
                    .stream()
                    .filter(ILibWidget::isVisible)
                    .forEach(w -> w.render(matrixStack, mouseX, mouseY, partialTicks));

            // Signal to the implementation, end render, after attached
            onRenderEnd(matrixStack, mouseX, mouseY, partialTicks);
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
            Minecraft.getInstance().player.closeScreen();
        }
    }

    protected void onRenderStart(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    protected void onRenderEnd(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public void update() {
        this.attachedWidgetsSupplier()
                .get()
                .forEach(ILibWidget::update);
    }

    @Override
    public void onMouseScrolled(LibVector2D vector2D, double delta) {
        widgetEventPropagator.onMouseScrolled(vector2D, delta);
    }

    @Override
    public void onClick(LibVector2D vector2D, int button) {
        widgetEventPropagator.onClick(vector2D, button);
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onClickRelease(LibVector2D vector2D, int button) {
        widgetEventPropagator.onClickRelease(vector2D, button);
    }

    public final AtomicReference<ILibWidget> attachWidget(ILibWidget widget) {
        synchronized (lockWidgetAttachment) {
            AtomicReference<ILibWidget> reference = new AtomicReference<>(widget);
            this.attachedWidgets.add(reference);

            return reference;
        }
    }

    public final void detachWidget(ILibWidget widget) {
        synchronized (lockWidgetAttachment) {
            AtomicReference<ILibWidget> iLibWidgetAtomicReference = this.attachedWidgets
                    .stream()
                    .filter(r -> widget.equals(r.get()))
                    .findFirst()
                    .orElse(null);
            if (iLibWidgetAtomicReference != null) {
                this.attachedWidgets.remove(iLibWidgetAtomicReference);
                // Not set to null just not visible, if is properly controlled the reference will be removed by GC
                iLibWidgetAtomicReference.get().setVisible(false);
            }
        }
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        this.hovering.set(true);
        this.widgetEventPropagator.onHover(vector2D);
    }

    @Override
    public void onHoverOut() {
        this.hovering.set(false);
        this.widgetEventPropagator.onHoverOut();
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        widgetEventPropagator.charTyped(codePoint, modifiers);
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return dimensions;
    }

    @Override
    public void onTokenReceived(FocusToken token) {
        widgetEventPropagator.onTokenReceived(token);
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = vectorBox;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public LibVectorBox getDimensions() {
        return dimensions;
    }

    @Override
    public int getZ() {
        return z;
    }

    public void setDimensions(LibVectorBox dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    protected final Supplier<List<ILibWidget>> attachedWidgetsSupplier() {
        synchronized (lockWidgetAttachment) {
            final List<AtomicReference<ILibWidget>> finalAttachedWidgets = new LinkedList<>(this.attachedWidgets);
            return () -> finalAttachedWidgets.stream().map(AtomicReference::get).collect(Collectors.toList());
        }
    }
}
