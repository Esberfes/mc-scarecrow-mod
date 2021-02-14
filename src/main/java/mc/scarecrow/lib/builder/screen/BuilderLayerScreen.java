package mc.scarecrow.lib.builder.screen;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mc.scarecrow.lib.builder.LayerVector;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVector3D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.proxy.Proxy;
import mc.scarecrow.lib.screen.gui.screen.LibBaseScreen;
import mc.scarecrow.lib.screen.gui.widget.event.observer.LibObservable;
import mc.scarecrow.lib.screen.gui.widget.implementation.LibWidgetList;
import mc.scarecrow.lib.screen.gui.widget.implementation.LibWidgetPanel;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@OnlyIn(Dist.CLIENT)
public class BuilderLayerScreen extends LibBaseScreen {
    {
        ILibInstanceHandler.fire(this);
    }

    private Gson gson = new GsonBuilder().registerTypeAdapter(Integer.class, new TypeAdapter()).create();
    @LibInject
    private Logger logger;
    private List<BuilderLayerScreenGridCell> cells;
    private int selectedId;
    private final AtomicInteger currentLayer;
    private Map<Integer, TreeSet<LayerVector>> stored;

    public BuilderLayerScreen() {
        this.currentLayer = new AtomicInteger(0);
    }

    static class TypeAdapter implements JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.getAsInt();
        }
    }

    @Override
    protected void init() {
        super.init();
        final float bottomMenuHeight = 30F;
        final float rightItemListWidth = 100F;
        final float cellSize = 12F;
        final float margin = 1F;
        final float cellTotalSize = cellSize + (margin * 2);
        final float totalBottomMenuHeight = bottomMenuHeight + (margin * 2);
        final float totalRightItemListWidth = rightItemListWidth + (margin * 2);
        this.cells = new LinkedList<>();
        this.stored = new LinkedHashMap<>();
        PlayerEntity playerEntity = Proxy.PROXY.getPlayerEntity();

        LibVector2D gameBounds = new LibVector2D(
                Minecraft.getInstance().getMainWindow().getScaledWidth(),
                Minecraft.getInstance().getMainWindow().getScaledHeight());

        this.width = (int) (gameBounds.getX() * 90 / 100);
        this.height = (int) (gameBounds.getY() * 85 / 100);
        LibVector2D bounds = new LibVector2D(this.width, this.height);
        float moveToTop = (gameBounds.getY() * 5 / 100);

        LibVectorBox box = new LibVectorBox(
                (gameBounds.getX() - this.width) / 2,
                ((gameBounds.getX() - this.width) / 2) + this.width,
                (gameBounds.getY() - this.height) / 2,
                ((gameBounds.getY() - this.height) / 2) + this.height
        ).move(0F, -moveToTop);

        int xElements = (int) ((box.relative().getWight() - rightItemListWidth) / cellTotalSize);
        int yElements = (int) ((box.getHeight() - totalBottomMenuHeight) / cellTotalSize);

        // Grd container
        LibVectorBox gridBox = box.relative()
                .withSizeToBottom(box.getHeight() - totalBottomMenuHeight)
                .withSizeToRight(box.getWight() - totalRightItemListWidth);
        this.addWidget(new LibWidgetPanel(gridBox, 1, 255, 255, 255, 0F));

        this.stored = read();
        TreeSet<LayerVector> layerVectors = stored.computeIfAbsent(BuilderLayerScreen.this.currentLayer.get(), (e) -> new TreeSet<>());

        // Grid cells
        LibVectorBox cellBox = box.relative().withSizeToRight(cellSize).withSizeToBottom(cellSize);
        LibVectorBox cellBoxMutate;
        for (int z = 0; z < yElements; z++) {
            cellBoxMutate = cellBox.relative().move(0, cellTotalSize * z);
            for (int x = 0; x < xElements; x++) {
                int finalX = x;
                int finalZ = z;
                LayerVector layerVector = layerVectors.stream()
                        .filter(l -> l.getPosition().equals(new LibVector3D((float) finalX, currentLayer.get(), finalZ)))
                        .findFirst().orElse(new LayerVector(x, BuilderLayerScreen.this.currentLayer.get(), z, Item.getIdFromItem(Items.AIR)));

                BuilderLayerScreenGridCell cell = new BuilderLayerScreenGridCell(cellBoxMutate, () -> Item.getItemById(selectedId), new LibObservable.Builder<LayerVector>()
                        .observer((libObservable, newValue) -> {
                            try {
                                stored = read();
                                synchronized (BuilderLayerScreen.this) {
                                    TreeSet<LayerVector> layerVectors1 = stored.computeIfAbsent(BuilderLayerScreen.this.currentLayer.get(), (e) -> new TreeSet<>());
                                    layerVectors1.remove(newValue);
                                    layerVectors1.add(newValue);
                                }
                                write(stored);
                            } catch (Throwable e) {
                                LogUtils.printError(logger, e);
                            }
                        })
                        .value(layerVector)
                        .build());
                cells.add(cell);
                cellBoxMutate = cellBoxMutate.move(cellTotalSize, 0);
                this.addWidget(cell);
                cell.init();
            }
        }

        // Right item list
        selectedId = Item.getIdFromItem(Items.AIR);
        LibWidgetList libWidgetList = new LibWidgetList(box.relative()
                .withSizeToLeft(100)
                .withSizeToBottom(gridBox.getHeight()),
                2, 225, 226, 225, 1, (libObservable, newValue) -> selectedId = newValue);

        this.addWidget(libWidgetList);
        libWidgetList.init();

        // Bottom menu
        LibVectorBox bottomMenuBox = box.relative().withSizeToTop(bottomMenuHeight);
        LibWidgetPanel bottomPanel = new LibWidgetPanel(bottomMenuBox, 1, 225, 226, 225, 1);
        this.addWidget(bottomPanel);
        bottomPanel.init();
    }

    @Override
    public void onClose() {
        super.onClose();
        write(this.stored);
    }

    private Map<Integer, TreeSet<LayerVector>> read() {
        synchronized (this) {
            try {
                File store = new File("./store");
                if (!store.exists())
                    return new LinkedHashMap<>();

                File save = new File(store.getAbsolutePath() + "/" + "save.json");

                if (!save.exists())
                    return new LinkedHashMap<>();

                try (FileInputStream fis = new FileInputStream(save)) {
                    StringBuilder buffer = new StringBuilder();
                    int character;
                    while ((character = fis.read()) != -1)
                        buffer.append((char) character);

                    Map<Integer, TreeSet<LayerVector>> o = gson.fromJson(buffer.toString(), new TypeToken<Map<Integer, TreeSet<LayerVector>>>() {
                    }.getType());
                    if (o != null)
                        return o;
                }

            } catch (Throwable e) {
                LogUtils.printError(logger, e);
            }

            return new LinkedHashMap<>();
        }
    }

    private void write(Map<Integer, TreeSet<LayerVector>> data) {
        synchronized (this) {
            try {
                File store = new File("./store");
                if (!store.exists())
                    if (!store.mkdir())
                        throw new Exception("Unable to create store dir");

                File save = new File(store.getAbsolutePath() + "/" + "save.json");
                if (!save.exists())
                    if (!save.createNewFile())
                        throw new Exception("Unable to create save file");

                try (FileWriter myWriter = new FileWriter(store.getAbsolutePath() + "/" + "save.json")) {
                    myWriter.write(new Gson().toJson(data, new TypeToken<Map<Integer, TreeSet<LayerVector>>>() {
                    }.getType()));
                }
            } catch (Throwable e) {
                LogUtils.printError(logger, e);
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        float scale = 0.65F;
        matrixStack.push();
        RenderSystem.pushMatrix();
        RenderSystem.scalef(scale, scale, 1F);
        float reposition = 1F / scale;
        float offSetY = 1F;
        for (BuilderLayerScreenGridCell cell : this.cells) {
            drawItemStack(Item.getItemById(cell.getItem()).getDefaultInstance(),
                    (int) (cell.getDimensionsBox().getLeftTop().getX() * reposition),
                    (int) ((cell.getDimensionsBox().getLeftTop().getY() * reposition) + offSetY));
        }

        RenderSystem.disableBlend();
        RenderSystem.popMatrix();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawItemStack(ItemStack stack, int x, int y) {
        this.itemRenderer.zLevel = 200.0F;
        net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = this.font;
        this.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, "");
        this.itemRenderer.zLevel = 0.0F;
    }
}
