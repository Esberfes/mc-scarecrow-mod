package mc.scarecrow.common.capability;

import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ScarecrowChunkCapability implements Capability.IStorage<ScarecrowChunkCapability> {

    private static final Logger LOGGER = LogManager.getLogger();

    private ServerWorld world;
    private final Map<ChunkPos, List<BlockPos>> chunksBlocks;

    public ScarecrowChunkCapability(ServerWorld world) {
        this.world = world;
        this.chunksBlocks = new HashMap<>();
    }

    public ScarecrowChunkCapability() {
        this.chunksBlocks = new HashMap<>();
    }

    public void add(ChunkPos chunk, BlockPos position) {
        synchronized (this) {
            try {
                if (this.chunksBlocks.containsKey(chunk) && this.chunksBlocks.get(chunk).contains(position))
                    return;

                if (!this.chunksBlocks.containsKey(chunk)) {
                    this.chunksBlocks.put(chunk, new LinkedList<>());
                    this.world.forceChunk(chunk.x, chunk.z, true);
                }

                this.chunksBlocks.get(chunk).add(position);

            } catch (Throwable e) {
                LogUtils.printError(LOGGER, e);
            }
        }
    }

    public void remove(ChunkPos chunk, BlockPos pos) {
        synchronized (this) {
            try {
                if (!this.chunksBlocks.containsKey(chunk) || !this.chunksBlocks.get(chunk).contains(pos))
                    return;

                if (this.chunksBlocks.get(chunk).size() == 1) {
                    this.world.forceChunk(chunk.x, chunk.z, false);
                    this.chunksBlocks.remove(chunk);
                } else
                    this.chunksBlocks.get(chunk).remove(pos);
            } catch (Throwable e) {
                LogUtils.printError(LOGGER, e);
            }
        }
    }

    public List<BlockPos> get(ChunkPos chunk) {
        synchronized (this) {
            return chunksBlocks.get(chunk);
        }
    }

    public Map<ChunkPos, List<BlockPos>> getAll() {
        synchronized (this) {
            return new HashMap<>(chunksBlocks);
        }
    }

    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public INBT writeNBT(Capability<ScarecrowChunkCapability> capability, ScarecrowChunkCapability instance, Direction side) {
        try {
            synchronized (this) {
                CompoundNBT compoundChunks = new CompoundNBT();
                for (Map.Entry<ChunkPos, List<BlockPos>> entry : instance.chunksBlocks.entrySet()) {
                    CompoundNBT chunkTag = new CompoundNBT();
                    chunkTag.putLong("chunk", entry.getKey().asLong());

                    LongArrayNBT blocks = new LongArrayNBT(entry.getValue().stream().map(BlockPos::toLong).collect(Collectors.toList()));
                    chunkTag.put("blocks", blocks);

                    compoundChunks.put(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
                }
                return compoundChunks;

            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return null;
        }
    }

    @Override
    public void readNBT(Capability<ScarecrowChunkCapability> capability, ScarecrowChunkCapability instance, Direction side, INBT nbt) {
        synchronized (this) {
            try {
                for (String key : ((CompoundNBT) nbt).keySet()) {
                    CompoundNBT chunkTag = ((CompoundNBT) nbt).getCompound(key);
                    ChunkPos chunk = new ChunkPos(chunkTag.getLong("chunk"));
                    LongArrayNBT blocks = (LongArrayNBT) chunkTag.get("blocks");
                    if (blocks != null)
                        Arrays.stream(blocks.getAsLongArray()).mapToObj(BlockPos::fromLong).forEach(pos -> instance.add(chunk, pos));
                }
            } catch (Throwable e) {
                LogUtils.printError(LOGGER, e);
            }
        }
    }
}
