package mc.scarecrow;

import mc.scarecrow.init.RegistryHandler;
import mc.scarecrow.network.ClientProxy;
import mc.scarecrow.network.IProxy;
import mc.scarecrow.network.Networking;
import mc.scarecrow.network.ServerProxy;
import mc.scarecrow.client.screens.ScarecrowScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod(MOD_IDENTIFIER)
public class ScarecrowMod {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ScarecrowMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onDedicatedServerSetup);

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
        Networking.registerMessages();
    }

    @SubscribeEvent
    public void serverLoad(FMLServerStartingEvent event) {
        LOGGER.debug("onCommonSetup");
    }

    @OnlyIn(Dist.CLIENT)
    private void setupClient(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(RegistryHandler.scarecrowBlockContainer.get(), ScarecrowScreen::new);
    }

}
