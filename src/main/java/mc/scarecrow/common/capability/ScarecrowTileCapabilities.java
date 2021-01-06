package mc.scarecrow.common.capability;

import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.ScarecrowTile;
import mc.scarecrow.common.entity.FakePlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScarecrowTileCapabilities {
    private static final Logger LOGGER = LogManager.getLogger();

    @CapabilityInject(ChunkTracker.class)
    public static Capability<ChunkTracker> TRACKER_CAPABILITY;
    private static final List<FakePlayerEntity> fakePlayers = new LinkedList<>();

    public static synchronized void addFakePlayer(FakePlayerEntity fakePlayerEntity) {
        fakePlayers.add(fakePlayerEntity);
    }

    public static synchronized void removeFakePlayer(FakePlayerEntity fakePlayerEntity) {
        fakePlayers.remove(fakePlayerEntity);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(ChunkTracker.class, new Capability.IStorage<ChunkTracker>() {
            public CompoundNBT writeNBT(Capability<ChunkTracker> capability, ChunkTracker instance, Direction side) {
                return instance.write();
            }

            public void readNBT(Capability<ChunkTracker> capability, ChunkTracker instance, Direction side, INBT nbt) {
                instance.read((CompoundNBT) nbt);
            }
        }, ChunkTracker::new);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e) {
        World world = e.getObject();
        if (world.isRemote || !(world instanceof ServerWorld))
            return;

        LazyOptional<ChunkTracker> tracker = LazyOptional.of(() -> new ChunkTracker((ServerWorld) world));
        e.addCapability(new ResourceLocation(MOD_IDENTIFIER, "chunk_tracker"), new ICapabilitySerializable<INBT>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return cap == TRACKER_CAPABILITY ? tracker.cast() : LazyOptional.empty();
            }

            @Override
            public INBT serializeNBT() {
                return TRACKER_CAPABILITY.writeNBT(tracker.orElse(null), null);
            }

            @Override
            public void deserializeNBT(INBT nbt) {
                TRACKER_CAPABILITY.readNBT(tracker.orElse(null), null, nbt);
            }
        });
        e.addListener(tracker::invalidate);
    }

    public static class ChunkTracker {

        private final ServerWorld world;
        private final Map<ChunkPos, List<BlockPos>> chunks = new HashMap<>();
        private final Map<UUID, List<BlockPos>> owners = new HashMap<>();
        private final Map<BlockPos, List<BlockPos>> dummies = new HashMap<>();

        public ChunkTracker(ServerWorld world) {
            this.world = world;
        }

        public ChunkTracker() {
            this.world = null;
        }

        public void add(ChunkPos chunk, BlockPos loader) {
            if (this.chunks.containsKey(chunk) && this.chunks.get(chunk).contains(loader))
                return;

            if (!this.chunks.containsKey(chunk)) {
                this.chunks.put(chunk, new LinkedList<>());
                this.world.forceChunk(chunk.x, chunk.z, true);
            }

            this.world.getTileEntity(loader);

            this.chunks.get(chunk).add(loader);
        }

        public void add(BlockPos pos, BlockPos[] positions) {
            if (!this.dummies.containsKey(pos))
                this.dummies.put(pos, Arrays.asList(positions));
        }

        public void remove(BlockPos pos) {
            this.dummies.remove(pos);
        }

        public void remove(UUID uuid, BlockPos loader) {
            if (!this.owners.containsKey(uuid) || !this.owners.get(uuid).contains(loader))
                return;

            if (this.owners.get(uuid).size() == 1) {
                this.owners.remove(uuid);
            } else
                this.owners.get(uuid).remove(loader);
        }

        public void add(UUID uuid, BlockPos loader) {
            if (this.owners.containsKey(uuid) && this.owners.get(uuid).contains(loader))
                return;

            if (!this.owners.containsKey(uuid)) {
                this.owners.put(uuid, new LinkedList<>());
            }

            this.owners.get(uuid).add(loader);
        }

        public void remove(ChunkPos chunk, BlockPos loader) {
            if (!this.chunks.containsKey(chunk) || !this.chunks.get(chunk).contains(loader))
                return;

            if (this.chunks.get(chunk).size() == 1) {
                this.world.forceChunk(chunk.x, chunk.z, false);
                this.chunks.remove(chunk);
            } else
                this.chunks.get(chunk).remove(loader);
        }

        public List<BlockPos> getDummies(BlockPos pos) {
            return this.dummies.containsKey(pos) ? this.dummies.get(pos) : new ArrayList<>();
        }

        public CompoundNBT write() {
            CompoundNBT compound = new CompoundNBT();

            CompoundNBT compoundChunks = new CompoundNBT();
            for (Map.Entry<ChunkPos, List<BlockPos>> entry : this.chunks.entrySet()) {
                CompoundNBT chunkTag = new CompoundNBT();
                chunkTag.putLong("chunk", entry.getKey().asLong());

                LongArrayNBT blocks = new LongArrayNBT(entry.getValue().stream().map(BlockPos::toLong).collect(Collectors.toList()));
                chunkTag.put("blocks", blocks);

                compoundChunks.put(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
            }
            compound.put("chunkstag", compoundChunks);

            CompoundNBT compoundBlocks = new CompoundNBT();
            for (Map.Entry<UUID, List<BlockPos>> entry : this.owners.entrySet()) {
                CompoundNBT ownerTag = new CompoundNBT();
                ownerTag.putUniqueId("uuid", entry.getKey());

                LongArrayNBT blocks = new LongArrayNBT(entry.getValue().stream().map(BlockPos::toLong).collect(Collectors.toList()));
                ownerTag.put("blocks", blocks);

                compoundBlocks.put(entry.getKey().toString(), ownerTag);
            }
            compound.put("blockstag", compoundBlocks);

            CompoundNBT dummiesChunks = new CompoundNBT();
            for (Map.Entry<BlockPos, List<BlockPos>> entry : this.dummies.entrySet()) {
                CompoundNBT blockPosTag = new CompoundNBT();
                blockPosTag.putLong("pos", entry.getKey().toLong());

                LongArrayNBT blocks = new LongArrayNBT(entry.getValue().stream().map(BlockPos::toLong).collect(Collectors.toList()));
                blockPosTag.put("positions", blocks);

                dummiesChunks.put(String.valueOf(entry.getKey().toLong()), blockPosTag);
            }
            compound.put("dummiestag", dummiesChunks);

            return compound;
        }

        public void read(CompoundNBT compound) {
            for (String key : compound.keySet()) {
                switch (key) {
                    case "chunkstag":
                        CompoundNBT chunksTag = compound.getCompound(key);
                        for (String subkey : chunksTag.keySet()) {
                            CompoundNBT chunkTag = chunksTag.getCompound(subkey);
                            ChunkPos chunk = new ChunkPos(chunkTag.getLong("chunk"));
                            LongArrayNBT blocks = (LongArrayNBT) chunkTag.get("blocks");
                            Arrays.stream(blocks.getAsLongArray()).mapToObj(BlockPos::fromLong).forEach(pos -> this.add(chunk, pos));
                        }
                        break;
                    case "blockstag":
                        CompoundNBT ownersTag = compound.getCompound(key);
                        for (String subkey : ownersTag.keySet()) {
                            CompoundNBT ownerTag = compound.getCompound(subkey);
                            UUID uuid = ownerTag.getUniqueId("uuid");
                            LongArrayNBT blocks = (LongArrayNBT) ownerTag.get("blocks");
                            Arrays.stream(blocks.getAsLongArray()).mapToObj(BlockPos::fromLong).forEach(pos -> this.add(uuid, pos));
                        }
                        break;
                    case "dummiestag":
                        CompoundNBT dimmiesTag = compound.getCompound(key);
                        for (String subkey : dimmiesTag.keySet()) {
                            CompoundNBT dummieTag = dimmiesTag.getCompound(subkey);
                            BlockPos pos = BlockPos.fromLong(dummieTag.getLong("pos"));
                            LongArrayNBT positions = (LongArrayNBT) dummieTag.get("positions");
                            List<BlockPos> posList = Arrays.stream(positions.getAsLongArray()).mapToObj(BlockPos::fromLong).collect(Collectors.toList());
                            for (BlockPos p : posList) {
                                Block block = world.getBlockState(p).getBlock();
                                if (block instanceof ScarecrowBlock)
                                    ((ScarecrowBlock) block).setTilePos(pos);
                            }
                            dummies.put(pos, posList);
                        }
                        break;
                }
            }
        }


    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.world instanceof ServerWorld))
            return;

        ServerWorld world = (ServerWorld) e.world;
        ServerChunkProvider chunkProvider = world.getChunkProvider();
        int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        if (tickSpeed > 0) {
            world.getCapability(TRACKER_CAPABILITY).ifPresent(tracker -> {
                for (Map.Entry<ChunkPos, List<BlockPos>> entry : tracker.chunks.entrySet()) {
                    ChunkPos pos = entry.getKey();
                    if (anyActive(entry.getValue(), world)) {
                        world.forceChunk(pos.x, pos.z, true);
                        if (chunkProvider.chunkManager.getTrackingPlayers(pos, false).count() == 0)
                            world.tickEnvironment(world.getChunk(pos.x, pos.z), tickSpeed);
                    } else {
                        world.forceChunk(pos.x, pos.z, false);
                    }
                }
            });
        }
    }

    private static boolean anyActive(List<BlockPos> pos, ServerWorld world) {
        try {
            for (BlockPos p : pos) {
                TileEntity tileEntity = world.getTileEntity(p);
                if (tileEntity instanceof ScarecrowTile && ((ScarecrowTile) tileEntity).isActive())
                    return true;
            }
        } catch (Throwable e) {
            LOGGER.error(e);
            return false;
        }
        return false;
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ServerWorld) {
            fakePlayers.forEach(p -> {
                if (p != null)
                    ((ServerWorld) event.getWorld()).removePlayer(p);
            });
        }
    }
}