package mc.scarecrow;

import mc.scarecrow.common.init.RegistryHandler;
import mc.scarecrow.common.init.events.ClientEventHandler;
import mc.scarecrow.common.network.ClientProxy;
import mc.scarecrow.common.network.IProxy;
import mc.scarecrow.common.network.ServerProxy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod(MOD_IDENTIFIER)
public class ScarecrowMod {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ScarecrowMod() {
        LOGGER.debug("Initializing registry");
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);

        RegistryHandler.init();

        // Initialize client side handler
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(ClientEventHandler::onClientSetup));

        LOGGER.debug("Finishing registry");
    }
}
