package mc.scarecrow.lib.builder.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.math.LibVectorBoxFactory;
import mc.scarecrow.lib.screen.gui.LibBaseGui;
import mc.scarecrow.lib.screen.gui.widget.LibWidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@OnlyIn(Dist.CLIENT)
public class BuilderLayerScreen extends LibBaseGui {
    {
        ILibInstanceHandler.fire(this);
    }

    @LibInject
    private Logger logger;

    private LibVector2D bounds;
    private LibVector2D gameBounds;
    private LibVectorBox box;
    private List<BuilderLayerScreenGridCell> cells;

    private int cellSize = 10;
    private int xElements;
    private int yElements;
    private int startX;
    private int startY;

    public BuilderLayerScreen() {

    }


    @Override
    protected void init() {
        super.init();

        this.gameBounds = new LibVector2D(
                Minecraft.getInstance().getMainWindow().getScaledWidth(),
                Minecraft.getInstance().getMainWindow().getScaledHeight()
        );
        this.width = this.gameBounds.getX() * 90 / 100;
        this.height = this.gameBounds.getY() * 80 / 100;
        this.bounds = new LibVector2D(this.width, this.height);
        this.box = new LibVectorBox(
                (this.gameBounds.getX() - this.width) / 2,
                ((this.gameBounds.getX() - this.width) / 2) + this.width,
                (this.gameBounds.getY() - this.height) / 2,
                ((this.gameBounds.getY() - this.height) / 2) + this.height
        );

        LibVector2D buttonPos = LibVectorBoxFactory.TOP_RIGHT.build(this.box, 0);

        this.cells = new LinkedList<>();
        this.cellSize = 10;
        this.xElements = ((this.width - ((cellSize + 1) * 2)) / (cellSize + 1)) - 11;
        this.yElements = (this.height - ((cellSize + 1) * 2)) / (cellSize + 1);
        this.startX = this.box.getLeftTop().getX() + cellSize;
        this.startY = this.box.getLeftTop().getY() + cellSize;

        final int increment = cellSize + 2;
        LibVectorBox cellBox = this.box.relative().withSizeToRight(cellSize).withSizeToBottom(cellSize);
        LibVectorBox cellBoxMutate;
        for (int y = 0; y < yElements; y++) {
            cellBoxMutate = cellBox.relative().move(0, increment * y);
            for (int x = 0; x < xElements; x++) {
                BuilderLayerScreenGridCell cell = new BuilderLayerScreenGridCell(cellBoxMutate);
                cellBoxMutate = cellBoxMutate.move(increment, 0);
                this.addWidget(cell);
                cell.init();
            }
        }

        LibWidgetList libWidgetList = new LibWidgetList(
                this.box.relative()
                        .withSizeToLeft(100)
                        .move(-20, 0)
                        .withSizeToBottom((yElements * increment) - 2),
                2, 225, 226, 225, 1);

        this.addWidget(libWidgetList);
        libWidgetList.init();
    }

    @Override
    protected void onRenderWidgetsEnd(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
      /* VertexDrawerBuilder.builder()
                .vertex(this.box.getRightTop().getX(), this.box.getRightTop().getY(), 207, 216, 220, 0.8F)
                .vertex(this.box.getLeftTop().getX(), this.box.getLeftTop().getY(), 207, 216, 220, 0.8F)
                .vertex(this.box.getLeftBottom().getX(), this.box.getLeftBottom().getY(), 207, 216, 220, 0.8F)
                .vertex(this.box.getRightBottom().getX(), this.box.getRightBottom().getY(), 207, 216, 220, 0.8F)
                .draw();

        this.layerScreenScroll.render(matrixStack, mouseX, mouseY, partialTicks);*/
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    class BuilderLayerScreeScroll extends ScrollPanel {

        protected final int width;
        protected final int height;
        protected final int top;
        protected int right;
        protected int left;
        private final List<IReorderingProcessor> lines;

        protected float scrollDistance;
        private final int barWidth = 6;
        private final int barLeft;
        private boolean scrolling;

        public BuilderLayerScreeScroll(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left);
            List<String> collect = ForgeRegistries.ITEMS.getEntries().stream()
                    .filter(e -> e.getValue() instanceof BlockItem)
                    .map(e -> e.getValue().getName().getString() + "::" + Item.getIdFromItem(e.getValue()))
                    .collect(Collectors.toList());
            this.lines = resizeContent(collect);
            /*  this.client = client;*/
            this.width = width;
            this.height = height;
            this.top = top;
            this.left = left;

            this.barLeft = this.left + this.width - barWidth;
        }

        @Override
        protected int getContentHeight() {
            int height = 50;
            height += (lines.size() * font.FONT_HEIGHT);
            if (height < this.bottom - this.top - 8)
                height = this.bottom - this.top - 8;
            return height;
        }

        @Override
        protected int getScrollAmount() {
            return font.FONT_HEIGHT * 3;
        }


        private List<IReorderingProcessor> resizeContent(List<String> lines) {
            List<IReorderingProcessor> ret = new ArrayList<>();
            for (String line : lines) {
                if (line == null) {
                    ret.add(null);
                    continue;
                }

                ITextComponent chat = ForgeHooks.newChatWithLinks(line, false);
                int maxTextLength = this.width - 12;
                if (maxTextLength >= 0) {
                    ret.addAll(LanguageMap.getInstance().func_244260_a(font.getCharacterManager().func_238362_b_(chat, maxTextLength, Style.EMPTY)));
                }
            }
            return ret;
        }

        @Override
        protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {
            for (IReorderingProcessor line : lines) {
                if (line != null) {
                    RenderSystem.enableBlend();
                    BuilderLayerScreen.this.font.func_238407_a_(mStack, line, left + 5, relativeY, 0xFFFFFF);
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }
                relativeY += font.FONT_HEIGHT;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button))
                return true;

            this.scrolling = button == 0 && mouseX >= barLeft && mouseX < barLeft + barWidth;
            if (this.scrolling) {
                return true;
            }
            int mouseListY = ((int) mouseY) - this.top - this.getContentHeight() + (int) this.scrollDistance - border;
            if (mouseX >= left && mouseX <= right && mouseListY < 0) {
                return this.clickPanel(mouseX - left, mouseY - this.top + (int) this.scrollDistance - border, button);
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            if (super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_))
                return true;
            boolean ret = this.scrolling;
            this.scrolling = false;
            return ret;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (this.scrolling) {
                int maxScroll = height - getBarHeight();
                double moved = deltaY / maxScroll;
                this.scrollDistance += getMaxScroll() * moved;
                applyScrollLimits();
                return true;
            }
            return false;
        }

        private void applyScrollLimits() {
            int max = getMaxScroll();

            if (max < 0) {
                max /= 2;
            }

            if (this.scrollDistance < 0.0F) {
                this.scrollDistance = 0.0F;
            }

            if (this.scrollDistance > max) {
                this.scrollDistance = max;
            }
        }

        protected boolean clickPanel(double mouseX, double mouseY, int button) {
            return false;
        }

        private int getMaxScroll() {
            return this.getContentHeight() - (this.height - this.border);
        }

        private int getBarHeight() {
            int barHeight = (height * height) / this.getContentHeight();

            if (barHeight < 32) barHeight = 32;

            if (barHeight > height - border * 2)
                barHeight = height - border * 2;

            return barHeight;
        }
    }
}
