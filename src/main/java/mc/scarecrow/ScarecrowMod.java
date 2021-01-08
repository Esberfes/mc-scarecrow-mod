package mc.scarecrow;

import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.capability.ScarecrowCapabilities;
import mc.scarecrow.common.init.RegistryHandler;
import mc.scarecrow.common.network.ClientProxy;
import mc.scarecrow.common.network.IProxy;
import mc.scarecrow.common.network.ServerProxy;
import mc.scarecrow.utils.LogUtils;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod(MOD_IDENTIFIER)
@WailaPlugin(MOD_IDENTIFIER)
public class ScarecrowMod implements IWailaPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ScarecrowMod() {
        LOGGER.debug("Initializing registry");
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onCommonSetup);

        RegistryHandler.init();

        LOGGER.debug("Finishing registry");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        try {
            ScarecrowCapabilities.registerCapabilities();
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @Override
    public void register(IRegistrar iRegistrar) {
        iRegistrar.registerComponentProvider(RegistryHandler.scarecrowTileBlock.get().create(), TooltipPosition.TAIL, ScarecrowTile.class);
        iRegistrar.registerStackProvider(RegistryHandler.scarecrowTileBlock.get().create(), ScarecrowTile.class);
    }
}
