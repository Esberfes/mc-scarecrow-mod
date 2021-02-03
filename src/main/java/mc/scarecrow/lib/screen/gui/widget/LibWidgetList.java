package mc.scarecrow.lib.screen.gui.widget;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.builder.screen.TextBuffer;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.FixedWidthFontRenderer;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static mc.scarecrow.lib.utils.IterationUtils.forEachIndexed;

@SuppressWarnings("UnstableApiUsage")
public class LibWidgetList implements ILibWidget {
    {
        ILibInstanceHandler.fire(this);
    }

    @LibInject
    private Logger logger;

    private LibVectorBox dimensions;

    private int z;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    private List<ILibWidget> widgetList;
    private LibWidgetPanel mainPanelWidget;
    private LibWidgetPanel topBorderWidget;
    private LibWidgetPanel bottomBorderWidget;
    private LibWidgetButton prevButton;
    private LibWidgetButton nextButton;
    private LibWidgetTextInput inputSearch;

    private final int elementHeight = 8;
    private final int elementMargin = 2;

    private double sizeScroll;
    private double maxSizeScroll;
    private int selectedId;

    private final LibWidgetEventPropagator widgetEventPropagator;
    private ScheduledExecutorService scheduledExecutorService;
    final ILibWidgetEventListener eventListener = this;
    private final ListeningScheduledExecutorService listeningScheduledExecutorService
            = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor());

    public LibWidgetList(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        this.dimensions = dimensions;
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.widgetList = new LinkedList<>();

        this.widgetEventPropagator = new LibWidgetEventPropagator(this::getWidgetList);
    }

    @Override
    public void init() {
        try {
            this.scheduledExecutorService = Executors.newScheduledThreadPool(10);
            this.widgetList = new LinkedList<>();
            this.mainPanelWidget = new LibWidgetPanel(dimensions, this.z, this.red, this.green, this.blue, this.alpha);

            List<BlockItem> blockItems = ForgeRegistries.ITEMS
                    .getEntries()
                    .stream()
                    .filter(i -> i.getValue() instanceof BlockItem)
                    .map(i -> (BlockItem) i.getValue())
                    .collect(Collectors.toList());

            forEachIndexed(blockItems, (index, blockItem) -> {
                LibWidgetListElement libWidgetListElement = new LibWidgetListElement()
                        .setText(blockItem.getBlock().getRegistryName().getPath())
                        .setNumeration(String.valueOf(index))
                        .setOnClickAction(() -> LibWidgetList.this.setSelectedId(Item.getIdFromItem(blockItem.asItem())));

                libWidgetListElement.setZ(this.z + 1);
                libWidgetListElement.init();
                widgetList.add(libWidgetListElement);

            }, (b) -> b.getBlock().getRegistryName() != null);

            this.sizeScroll = widgetList.size() * elementHeight + (elementMargin * 2);
            this.maxSizeScroll = sizeScroll;

            this.topBorderWidget = new LibWidgetPanel(dimensions.relative().withSizeToBottom(elementHeight), this.z + 4, 245, 245, 246, 1);
            this.bottomBorderWidget = new LibWidgetPanel(dimensions.relative().withSizeToTop(elementHeight + (elementMargin * 4)), this.z + 4, 245, 245, 246, 1);

            this.prevButton = new LibWidgetButton(this.dimensions.relative()
                    .withSizeToTop(elementHeight)
                    .withSizeToRight((elementHeight - elementMargin) * 2)
                    .move(elementMargin, -elementMargin),
                    this.z + 3, 144, 164, 174, 1, LibWidgetArrowIcon.Direction.LEFT);
            this.prevButton.init();
            this.prevButton.setOnClickAction(() -> {
                final LibWidgetButton button = this.prevButton;
                listeningScheduledExecutorService.schedule(buttonTask(listeningScheduledExecutorService,
                        eventListener, button, -1.5D), 50, TimeUnit.MILLISECONDS);
            });

            this.nextButton = new LibWidgetButton(dimensions.relative()
                    .withSizeToTop(elementHeight)
                    .withSizeToLeft((elementHeight - elementMargin) * 2)
                    .move(-elementMargin, -elementMargin),
                    this.z + 5, 144, 164, 174, 1, LibWidgetArrowIcon.Direction.RIGHT);
            this.nextButton.init();
            this.nextButton.setOnClickAction(() -> {
                final LibWidgetButton button = this.nextButton;
                listeningScheduledExecutorService.schedule(buttonTask(listeningScheduledExecutorService,
                        eventListener, button, 1.5D), 50, TimeUnit.MILLISECONDS);
            });

            int inputWight = this.bottomBorderWidget.getDimensions().getWight() - (this.nextButton.getDimensionsBox().getWight() * 2) - (elementMargin * 6);
            this.inputSearch = new LibWidgetTextInput(this.nextButton.getDimensionsBox().relative()
                    .withSizeToLeft(inputWight)
                    .move(-((this.bottomBorderWidget.getDimensions().getWight() - inputWight)) / 2, 0),
                    this.z + 5, 144, 164, 174, 1);
            this.inputSearch.init();

            updateElementsAsync();

        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    private static Runnable buttonTask(ListeningScheduledExecutorService executorService,
                                       ILibWidgetEventListener libWidgetEventListener, LibWidgetButton button, double delta) {
        return () -> {
            libWidgetEventListener.onMouseScrolled(button.getDimensionsBox().getRightTop(), delta);
            if (button.getPressing()) {
                Runnable runnable = buttonTask(executorService, libWidgetEventListener, button, delta);
                executorService.schedule(runnable, 25, TimeUnit.MILLISECONDS);
            }
        };
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
        try {
            // Draw main scroll list box
            this.mainPanelWidget.render(matrixStack, mouseX, mouseY, partialTicks);

            // Draw all the elements that are marked by "visible"
            for (ILibWidget widget : getWidgets().stream().filter(ILibWidget::isVisible).collect(Collectors.toList()))
                widget.render(matrixStack, mouseX, mouseY, partialTicks);

            // Draw top border for scroll effect
            this.topBorderWidget.render(matrixStack, mouseX, mouseY, partialTicks);
            // Draw bottom border for scroll effect
            this.bottomBorderWidget.render(matrixStack, mouseX, mouseY, partialTicks);
            // Draw previous button
            this.prevButton.render(matrixStack, mouseX, mouseY, partialTicks);
            // Draw next button
            this.nextButton.render(matrixStack, mouseX, mouseY, partialTicks);
            // Draw input searxh items
            this.inputSearch.render(matrixStack, mouseX, mouseY, partialTicks);

            // Draw information about the selected item
            FixedWidthFontRenderer.drawString(
                    (float) dimensions.getLeftTop().getX() + elementMargin,
                    (float) dimensions.getLeftTop().getY() + elementMargin,
                    this.z + 6,
                    new TextBuffer("ID: " + this.selectedId));

            // Recalculate positions async
            updateElementsAsync();
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    private void updateElementsAsync() {
        CompletableFuture.runAsync(() -> {
            int increment = elementMargin + elementHeight;
            double diffScroll = maxSizeScroll - sizeScroll;

            LibVectorBox widgetBox = new LibVectorBox(
                    dimensions.getLeftTop().getX() + elementMargin,
                    dimensions.getRightTop().getX() - elementMargin,
                    dimensions.getRightTop().getY() + elementHeight + elementMargin - (int) diffScroll,
                    dimensions.getRightTop().getY() + elementHeight + increment - (int) diffScroll);

            for (ILibWidget widget : getWidgets()) {
                widget.setDimensionsBox(widgetBox);

                // Check if element should render, will not render if its out of bounds
                widget.setVisible(widgetBox.getLeftTop().getY() > dimensions.getLeftTop().getY()
                        && widgetBox.getLeftBottom().getY() < dimensions.getLeftBottom().getY());

                // Apply increment for next element
                widgetBox = widgetBox.relative().move(0, increment);
            }
        });
    }

    private synchronized List<ILibWidget> getWidgets() {
        return new ArrayList<>(this.widgetList);
    }

    public void setSelectedId(int selectedId) {
        this.selectedId = selectedId;
    }

    @Override
    public void onMouseScrolled(LibVector2D vector2D, double delta) {
        if (this.dimensions.isCollisionTo(vector2D)) {
            this.sizeScroll -= delta * 3;
            this.sizeScroll = Math.min(this.sizeScroll, maxSizeScroll);
            this.sizeScroll = Math.max(this.sizeScroll, 0);
        }
    }

    @Override
    public void onClick(LibVector2D vector2D, int button) {
        widgetEventPropagator.onClick(vector2D, button);
    }

    @Override
    public void onClickRelease(LibVector2D vector2D, int button) {
        widgetEventPropagator.onClickRelease(vector2D, button);
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        widgetEventPropagator.onHover(vector2D);
    }

    @Override
    public void onHoverOut() {
        widgetEventPropagator.onHoverOut();
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

    // Add extra widget to the listener
    private List<ILibWidget> getWidgetList() {
        List<ILibWidget> copy = new ArrayList<>(this.widgetList);
        copy.addAll(Arrays.asList(this.nextButton, this.prevButton, this.inputSearch));

        return copy;
    }
}
