package mc.scarecrow.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScarecrowCapabilities {

    @CapabilityInject(ScarecrowChunkCapability.class)
    public static Capability<ScarecrowChunkCapability> CHUNK_CAPABILITY;

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ScarecrowChunkCapability.class, new ScarecrowChunkCapability(), ScarecrowChunkCapability::new);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e) {
        World world = e.getObject();
        if (!world.isRemote() && (world instanceof ServerWorld))
            attach(e, CHUNK_CAPABILITY, new ScarecrowChunkCapability((ServerWorld) world));
    }

    @SuppressWarnings({"all", "rawtypes"})
    private static <T> void attach(AttachCapabilitiesEvent<World> e, Capability<T> capability, T capabilityInstance) {
        LazyOptional<T> tracker = LazyOptional.of(() -> capabilityInstance);

        e.addCapability(new ResourceLocation(MOD_IDENTIFIER, "chunk_capability"), new ICapabilitySerializable<INBT>() {
            @Override
            public <E> LazyOptional<E> getCapability(Capability<E> cap, Direction side) {
                return cap == capability ? tracker.cast() : LazyOptional.empty();
            }

            @Override
            public INBT serializeNBT() {
                return capability.writeNBT(tracker.orElse(null), null);
            }

            @Override
            public void deserializeNBT(INBT nbt) {
                capability.readNBT(tracker.orElse(null), null, nbt);
            }
        });
    }
}
