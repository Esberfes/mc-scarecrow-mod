package mc.scarecrow.common.init.events;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerStartingEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.debug("onServerStarting");
    }
}
