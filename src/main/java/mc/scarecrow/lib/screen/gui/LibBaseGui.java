package mc.scarecrow.lib.screen.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.FocusToken;
import mc.scarecrow.lib.screen.gui.widget.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.LibWidgetEventPropagator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LibBaseGui extends Screen {
    {
        ILibInstanceHandler.fire(this);
    }

    protected LibVectorBox dimensions;
    private List<ILibWidget> widgets;
    private final LibWidgetEventPropagator widgetEventPropagator;
    private final FocusToken focusToken;
    private LibVector2D focusPosition;

    @LibInject
    private Logger logger;

    protected LibBaseGui() {
        super(new StringTextComponent(""));
        this.dimensions = new LibVectorBox(0, 0, 0, 0);
        this.widgets = new ArrayList<>();
        this.widgetEventPropagator = new LibWidgetEventPropagator(this::getWidgetList);
        this.focusPosition = new LibVector2D();
        this.focusToken = new FocusToken(() -> this.focusPosition);
    }

    private List<ILibWidget> getWidgetList() {
        return new ArrayList<>(this.widgets);
    }

    protected final void addWidget(ILibWidget widget) {
        this.widgets.add(widget);
    }

    @Override
    protected void init() {
        this.dimensions = new LibVectorBox(
                0, Minecraft.getInstance().getMainWindow().getScaledWidth(),
                0, Minecraft.getInstance().getMainWindow().getScaledHeight());

        this.width = 0;
        this.height = 0;

        List<ILibWidget> toInit = new LinkedList<>(this.widgets);
        this.widgets = new ArrayList<>();
        for (ILibWidget widget : toInit)
            widget.init();
    }

    @Override
    public final void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (ILibWidget widget : widgets)
            if (widget.isVisible())
                widget.render(matrixStack, mouseX, mouseY, partialTicks);

        onRenderWidgetsEnd(matrixStack, mouseX, mouseY, partialTicks);
    }

    protected void onRenderWidgetsEnd(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.widgetEventPropagator.onMouseScrolled(new LibVector2D(mouseX, mouseY), delta);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.widgetEventPropagator.onClick(new LibVector2D(mouseX, mouseY), button);
        this.focusPosition = new LibVector2D(mouseX, mouseY);
        this.widgetEventPropagator.onTokenReceived(this.focusToken);

        return true;
    }

    @Override
    public void mouseMoved(double xPos, double mouseY) {
        this.widgetEventPropagator.onHover(new LibVector2D(xPos, mouseY));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.widgetEventPropagator.onClickRelease(new LibVector2D(mouseX, mouseY), button);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // TODO
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.widgetEventPropagator.charTyped(codePoint, modifiers);
        return true;
    }

    public LibVectorBox getDimensions() {
        return dimensions;
    }
}
