package mc.scarecrow;

import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.capability.ScarecrowCapabilities;
import mc.scarecrow.lib.core.LibCore;
import mc.scarecrow.lib.register.LibAutoRegister;
import mc.scarecrow.lib.utils.LogUtils;
import mcp.mobius.waila.api.*;
import net.minecraftforge.eventbus.api.IEventBus;
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

    public ScarecrowMod() {
        LOGGER.debug("Initializing Scarecrow Mod");

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LibCore.initialize(FMLJavaModLoadingContext.get(), MOD_IDENTIFIER);

        modEventBus.addListener(this::onCommonSetup);
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
        iRegistrar.registerComponentProvider((IComponentProvider) LibAutoRegister.TILE_ENTITIES.get("scarecrow").create(),
                TooltipPosition.TAIL, ScarecrowTile.class);
    }
}
