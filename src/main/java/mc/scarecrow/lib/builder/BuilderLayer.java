package mc.scarecrow.lib.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mc.scarecrow.lib.builder.listener.BuildLayerOnBlockListener;
import mc.scarecrow.lib.builder.listener.BuildLayerOnFinishListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BuilderLayer {

    private static final int DEFAULT_DELAY = 300;

    private Map<Integer, Set<LayerVector>> blocksLayers;
    private int delay;

    // Avoid serialization
    private transient ServerWorld world;
    private transient ScheduledExecutorService scheduledExecutorService;
    private transient BuildLayerOnBlockListener onBlockListener;
    private transient BuildLayerOnFinishListener onFinishListener;

    public BuilderLayer(ServerWorld world) {
        this(world, DEFAULT_DELAY);
    }

    public BuilderLayer(ServerWorld world, int delay) {
        this.world = world;
        this.blocksLayers = new TreeMap<>(Integer::compareTo);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.delay = delay;
    }

    public void setOnBlockListener(BuildLayerOnBlockListener onBlockListener) {
        this.onBlockListener = onBlockListener;
    }

    public void setOnFinishListener(BuildLayerOnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public void put(int layer, LayerVector layerVector) {
        Set<LayerVector> layerVectors = blocksLayers.computeIfAbsent(layer,
                (e) -> new TreeSet<>(LayerVector::compareTo));

        layerVectors.add(layerVector);
    }

    private final Object lock = new Object();

    public synchronized void build(BlockPos fromPos) throws ExecutionException, InterruptedException {
        AtomicInteger delay = new AtomicInteger(this.delay);
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<Integer, Set<LayerVector>> layer : blocksLayers.entrySet()) {
            for (LayerVector layerVector : layer.getValue()) {
                BlockPos relativeBlockPos = layerVector.toRelativeBlockPos(
                        fromPos.getX(),
                        fromPos.getY() + layer.getKey(),
                        fromPos.getZ()
                );
                int currentDelay = delay.addAndGet(this.delay);

                futures.add(scheduledExecutorService.schedule(() -> {
                    BlockState blockState = fromItem(getItemById(layerVector.getItemId()));
                    world.setBlockState(relativeBlockPos, blockState, 3);

                    if (onBlockListener != null)
                        onBlockListener.onBlock(relativeBlockPos, blockState);

                }, currentDelay, TimeUnit.MILLISECONDS));
            }
        }
        if (onFinishListener != null) {
            for (Future<?> future : futures)
                future.get();
            onFinishListener.onFinish();
        }
    }

    public void fillTest() {
        for (int y = 1; y <= 3; y++) {
            for (int x = 0; x < 10; x++) {
                for (int z = 0; z < 10; z++) {
                    put(y, new LayerVector(x, z, getIdByItem(fromItem(Items.ACACIA_LOG).getBlock().asItem())));
                }
            }
        }
    }

    public String toJson(boolean pretty) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static BuilderLayer fromJson(String json) {
        return new Gson().fromJson(json, BuilderLayer.class);
    }

    public static BlockState fromItem(Item item) {
        return Block.getBlockFromItem(item).getDefaultState();
    }

    public static int getIdByItem(Item item) {
        return Item.getIdFromItem(item);
    }

    public static Item getItemById(int id) {
        return Item.getItemById(id);
    }
}
/**
 * BuilderLayer a = new BuilderLayer(world);
 * a.fillTest();
 * a.build(getPos());
 **/