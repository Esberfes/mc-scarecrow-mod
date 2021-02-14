package mc.scarecrow.common.capability;

import mc.scarecrow.lib.utils.LogUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScarecrowCapabilities {

    private static final Logger LOGGER = LogManager.getLogger();

    @CapabilityInject(ScarecrowChunkCapability.class)
    public static Capability<ScarecrowChunkCapability> CHUNK_CAPABILITY;

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ScarecrowChunkCapability.class, new ScarecrowChunkCapability(), ScarecrowChunkCapability::new);
    }

    @SubscribeEvent
    public static void attachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (!world.isRemote() && (world instanceof ServerWorld)) {
            attach(event, CHUNK_CAPABILITY, new ScarecrowChunkCapability((ServerWorld) world), "chunk_capability");
        }
    }

    @SuppressWarnings({"all", "rawtypes"})
    private static <T> void attach(AttachCapabilitiesEvent<?> event, Capability<T> capability, T capabilityInstance, String identifier) {
        try {
            LazyOptional<T> tracker = LazyOptional.of(() -> capabilityInstance);

            event.addCapability(new ResourceLocation(MOD_IDENTIFIER, identifier), new ICapabilitySerializable<INBT>() {
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
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }
}
