package mc.scarecrow;

import mc.scarecrow.common.block.ScarecrowContainer;
import mc.scarecrow.common.capability.ScarecrowTileCapabilities;
import mc.scarecrow.common.init.RegistryHandler;
import mc.scarecrow.common.network.ClientProxy;
import mc.scarecrow.common.network.IProxy;
import mc.scarecrow.common.network.Networking;
import mc.scarecrow.common.network.ServerProxy;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod(MOD_IDENTIFIER)
public class ScarecrowMod {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    // TODO mover a client side
    public static final boolean IS_DEV_MODE = true;

    @ObjectHolder(MOD_IDENTIFIER + ":scarecrow_block")
    public static ContainerType<ScarecrowContainer> TYPE = null;


    public ScarecrowMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onDedicatedServerSetup);

        MinecraftForge.EVENT_BUS.register(new Register());
        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // Client setup
            modEventBus.addListener(this::setupClient);
        });

        RegistryHandler.init();
    }

    public void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        LOGGER.debug("onDedicatedServerSetup");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.debug("onCommonSetup");
        ScarecrowTileCapabilities.register();
        Networking.registerMessages();
    }

    @SubscribeEvent
    public void serverLoad(FMLServerStartingEvent event) {
        LOGGER.debug("onCommonSetup");
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        @SubscribeEvent
        public static void onContainerRegistry(RegistryEvent.Register<ContainerType<?>> event) {
            IForgeRegistry<ContainerType<?>> r = event.getRegistry();
            r.register(IForgeContainerType.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        return new ScarecrowContainer(windowId, PROXY.getPlayerWorld(), pos, inv, PROXY.getPlayerEntity());
                    }
            ).setRegistryName("scarecrow_block"));
        }
    }

    // TODO mover a cliente side
    @OnlyIn(Dist.CLIENT)
    private void setupClient(FMLClientSetupEvent event) {
        PROXY.init();
    }
}
