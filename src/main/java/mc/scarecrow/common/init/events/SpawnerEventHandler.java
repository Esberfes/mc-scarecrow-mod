package mc.scarecrow.common.init.events;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnerEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCheckSpawnerSpawn(LivingSpawnEvent.CheckSpawn event) {
        LOGGER.debug("onCheckSpawnerSpawn: " + event.getEntityLiving().getPosition().getCoordinatesAsString());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPotentialSpawns(WorldEvent.PotentialSpawns event) {
        LOGGER.debug("onPotentialSpawns");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSummonAidEvent(ZombieEvent.SummonAidEvent event) {
        LOGGER.debug("onSummonAidEvent");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        //LOGGER.debug("onWorldTick");
    }
}
