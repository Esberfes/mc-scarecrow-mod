package mc.scarecrow.lib.screen.gui.widget.implementation;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.render.font.LibScaledFontRenderer;
import mc.scarecrow.lib.screen.gui.render.font.LibTextBuffer;
import mc.scarecrow.lib.screen.gui.widget.base.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.base.icon.IconDirection;
import mc.scarecrow.lib.screen.gui.widget.event.ILibWidgetEventListener;
import mc.scarecrow.lib.screen.gui.widget.event.LibWidgetEventPropagator;
import mc.scarecrow.lib.screen.gui.widget.event.observer.LibObservable;
import mc.scarecrow.lib.screen.gui.widget.event.observer.LibObserver;
import mc.scarecrow.lib.screen.gui.widget.focus.FocusToken;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
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
    private LibVectorBox visibleBox;

    private int z;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    private List<ILibWidget> widgetList;
    private List<ILibWidget> activeWidgetList;
    private LibWidgetPanel mainPanelWidget;
    private LibWidgetPanel topBorderWidget;
    private LibWidgetPanel bottomBorderWidget;
    private LibWidgetButton prevButton;
    private LibWidgetButton nextButton;
    private LibWidgetTextInput inputSearch;

    private final int ELEMENT_HEIGHT = 8;
    private final int ELEMENT_MARGIN = 2;

    private final AtomicDouble currentSizeScroll;
    private final AtomicDouble maxSizeScroll;

    private final LibWidgetEventPropagator widgetEventPropagator;
    final ILibWidgetEventListener eventListener = this;
    private final ListeningScheduledExecutorService listeningScheduledExecutorService
            = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor());
    private String filter;
    private final Object lock = new Object();
    private LibObservable<Integer> itemId;

    public LibWidgetList(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha, LibObserver<Integer> itemIdObserver) {
        this.dimensions = dimensions;
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.currentSizeScroll = new AtomicDouble();
        this.maxSizeScroll = new AtomicDouble();
        AtomicDouble minSizeScroll = new AtomicDouble();
        this.widgetList = new LinkedList<>();
        this.activeWidgetList = new LinkedList<>();
        this.widgetEventPropagator = new LibWidgetEventPropagator(this::getWidgetList);
        this.itemId = new LibObservable.Builder<Integer>().value(Item.getIdFromItem(Items.AIR)).observer(itemIdObserver).build();
    }

    @Override
    public void init() {
        try {
            this.widgetList = new LinkedList<>();
            this.mainPanelWidget = new LibWidgetPanel(dimensions, this.z, this.red, this.green, this.blue, this.alpha);

            this.widgetList.addAll(elementsSupplier().get());

            this.topBorderWidget = new LibWidgetPanel(dimensions.relative().withSizeToBottom(ELEMENT_HEIGHT), this.z + 4, 245, 245, 246, 1);
            this.bottomBorderWidget = new LibWidgetPanel(dimensions.relative().withSizeToTop(ELEMENT_HEIGHT + (ELEMENT_MARGIN * 4)), this.z + 4, 245, 245, 246, 1);

            this.visibleBox = new LibVectorBox(
                    topBorderWidget.getDimensionsBox().getLeftTop(),
                    bottomBorderWidget.getDimensionsBox().getLeftBottom(),
                    topBorderWidget.getDimensionsBox().getRightTop(),
                    bottomBorderWidget.getDimensionsBox().getRightBottom()
            );

            this.prevButton = new LibWidgetButton(this.dimensions.relative()
                    .withSizeToTop(ELEMENT_HEIGHT)
                    .withSizeToRight((ELEMENT_HEIGHT - ELEMENT_MARGIN) * 2)
                    .move(ELEMENT_MARGIN, -ELEMENT_MARGIN),
                    this.z + 3, 144, 164, 174, 1, IconDirection.LEFT);
            this.prevButton.init();
            this.prevButton.setOnClickAction(() -> {
                final LibWidgetButton button = this.prevButton;
                listeningScheduledExecutorService.schedule(buttonTask(listeningScheduledExecutorService,
                        eventListener, button, +1.5D), 50, TimeUnit.MILLISECONDS);
            });

            this.nextButton = new LibWidgetButton(dimensions.relative()
                    .withSizeToTop(ELEMENT_HEIGHT)
                    .withSizeToLeft((ELEMENT_HEIGHT - ELEMENT_MARGIN) * 2)
                    .move(-ELEMENT_MARGIN, -ELEMENT_MARGIN),
                    this.z + 5, 144, 164, 174, 1, IconDirection.RIGHT);
            this.nextButton.init();
            this.nextButton.setOnClickAction(() -> {
                final LibWidgetButton button = this.nextButton;
                listeningScheduledExecutorService.schedule(buttonTask(listeningScheduledExecutorService,
                        eventListener, button, -1.5D), 50, TimeUnit.MILLISECONDS);
            });

            float inputWight = this.bottomBorderWidget.getDimensionsBox().getWight() - (this.nextButton.getDimensionsBox().getWight() * 2) - (ELEMENT_MARGIN * 6);
            this.inputSearch = new LibWidgetTextInput(this.nextButton
                    .getDimensionsBox()
                    .relative()
                    .withSizeToLeft(inputWight)
                    .centered(this.bottomBorderWidget.getDimensionsBox())
                    .move(0, ELEMENT_MARGIN),
                    this.z + 5, 144, 164, 174, 1,
                    (libObservable, newValue) -> filter = newValue.toString());
            this.inputSearch.init();

            updateElementsAsync();

        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    private Supplier<List<ILibWidget>> elementsSupplier() {
        return () -> {
            List<ILibWidget> result = new LinkedList<>();

            List<BlockItem> blockItems = ForgeRegistries.ITEMS
                    .getEntries()
                    .stream()
                    .filter(i -> i.getValue() instanceof BlockItem)
                    .map(i -> (BlockItem) i.getValue())
                    .collect(Collectors.toList());

            forEachIndexed(blockItems, (index, blockItem) -> {
                LibWidgetListElement libWidgetListElement = new LibWidgetListElement()
                        .setText(blockItem.getName().getString())
                        .setNumeration(String.valueOf(index))
                        .setOnClickAction(() -> LibWidgetList.this.itemId.set(Item.getIdFromItem(blockItem.asItem())));

                libWidgetListElement.setZ(z + 1);
                libWidgetListElement.init();
                result.add(libWidgetListElement);

            }, (b) -> b.getBlock().getRegistryName() != null);

            return result;
        };
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
            for (ILibWidget widget : getActiveVisibleScrollWidgets())
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
            LibScaledFontRenderer.drawString(
                    (float) dimensions.getLeftTop().getX() + ELEMENT_MARGIN,
                    (float) dimensions.getLeftTop().getY() + ELEMENT_MARGIN,
                    this.z + 6,
                    new LibTextBuffer("ID: " + this.itemId.get() + " - Count: " + getFilteredScrollWidgets().size()));

            // Recalculate positions async
            updateElementsAsync();
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    private final AtomicBoolean updating = new AtomicBoolean(false);

    public synchronized void recalculateScrollSizes(int count) {
        float ELEMENT_TOTAL_HEIGHT = (float) ELEMENT_HEIGHT + ((float) ELEMENT_MARGIN * 2);
        float maxVisibleElements = (this.dimensions.getHeight()) / ELEMENT_TOTAL_HEIGHT;
        float elements = Math.max(count - maxVisibleElements, 0);
        this.maxSizeScroll.set(elements * ELEMENT_TOTAL_HEIGHT);
        this.currentSizeScroll.set(0);
    }

    private void updateElementsAsync() {
        if (updating.get())
            return;

        this.updating.set(true);

        CompletableFuture.runAsync(() -> {
            try {
                List<ILibWidget> activeWidgetList = getFilteredScrollWidgets();

                if (activeWidgetList.size() != this.activeWidgetList.size())
                    recalculateScrollSizes(activeWidgetList.size());

                int increment = ELEMENT_MARGIN + ELEMENT_HEIGHT;

                LibVectorBox widgetBox = new LibVectorBox(
                        dimensions.getLeftTop().getX() + ELEMENT_MARGIN,
                        dimensions.getRightTop().getX() - ELEMENT_MARGIN,
                        dimensions.getRightTop().getY() + ELEMENT_HEIGHT + ELEMENT_MARGIN - (int) currentSizeScroll.get(),
                        dimensions.getRightTop().getY() + ELEMENT_HEIGHT + increment - (int) currentSizeScroll.get());

                for (ILibWidget widget : activeWidgetList) {
                    // if filter and not apply discarded from position calculation
                    widget.setDimensionsBox(widgetBox);

                    // Check if is inside of visual box
                    widget.setVisible(widgetBox.isInsideTo(this.visibleBox));

                    widgetBox = widgetBox.relative().move(0, increment);
                }

                this.addActiveScrollWidget(activeWidgetList);

            } catch (Throwable e) {
                LogUtils.printError(logger, e);
            } finally {
                this.updating.set(false);
            }
        });
    }

    private void addActiveScrollWidget(List<ILibWidget> widgets) {
        synchronized (lock) {
            this.activeWidgetList = new LinkedList<>(widgets);
        }
    }

    private List<ILibWidget> getActiveVisibleScrollWidgets() {
        synchronized (lock) {
            return this.activeWidgetList.stream().filter(ILibWidget::isVisible).collect(Collectors.toList());
        }
    }

    private List<ILibWidget> getFilteredScrollWidgets() {
        synchronized (lock) {
            return this.widgetList.stream()
                    .filter(w -> StringUtils.isBlank(filter) || ((LibWidgetListElement) w).getText().toLowerCase().contains(filter.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public synchronized void onMouseScrolled(LibVector2D vector2D, double delta) {
        if (this.dimensions.isCollisionTo(vector2D)) {
            double deltaInc = this.currentSizeScroll.get();
            deltaInc -= delta * 3;
            deltaInc = Math.max(deltaInc, 0);
            deltaInc = Math.min(deltaInc, this.maxSizeScroll.get());
            this.currentSizeScroll.set(deltaInc);
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
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        widgetEventPropagator.keyPressed(keyCode, scanCode, modifiers);
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
        synchronized (lock) {
            List<ILibWidget> copy = new LinkedList<>(getActiveVisibleScrollWidgets());
            copy.addAll(Arrays.asList(this.nextButton, this.prevButton, this.inputSearch));

            return copy;
        }
    }
}
