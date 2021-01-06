package mc.scarecrow.common.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScarecrowChunkCapability implements IScarecrowCapability<ChunkPos, List<BlockPos>, Map<ChunkPos, List<BlockPos>>> {

    private final ServerWorld world;
    private final Map<ChunkPos, List<BlockPos>> chunksBlocks;

    public ScarecrowChunkCapability(ServerWorld world) {
        this.world = world;
        this.chunksBlocks = new HashMap<>();
    }

    @Override
    public synchronized void add(ChunkPos key, List<BlockPos> value) {

    }

    @Override
    public synchronized void remove(ChunkPos key) {

    }

    @Override
    public synchronized void remove(ChunkPos key, List<BlockPos> value) {

    }

    @Override
    public synchronized List<BlockPos> get(ChunkPos key) {
        return null;
    }

    @Override
    public synchronized Map<ChunkPos, List<BlockPos>> getAll() {
        return new HashMap<>(chunksBlocks);
    }

    @Override
    public synchronized void removeAll() {

    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public synchronized CompoundNBT write() {
        return null;
    }

    @Override
    public synchronized void read(CompoundNBT compound) {

    }
}
