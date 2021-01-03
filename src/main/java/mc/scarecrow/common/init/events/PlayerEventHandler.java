package mc.scarecrow.common.init.events;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onPlayerEventHandler(PlayerEvent event) {
        //LOGGER.debug("onEntityJoinWorld");
    }

    @SubscribeEvent
    public static void onEntityAdded(PlayerEvent.PlayerLoggedInEvent event)
    {
        LOGGER.debug("onEntityAdded");
    }
}
