package mc.scarecrow.client.init;

import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.client.particle.ScarecrowParticleFactory;
import mc.scarecrow.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistryHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    public static ClientRegistry clientRegistry;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        try {
            ScarecrowMod.PROXY.init();
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    public static void init() {
        clientRegistry = new ClientRegistry();
    }

    public static class ClientRegistry {

        public static BasicParticleType scarecrowParticle = new BasicParticleType(true);

        public ClientRegistry() {
            MinecraftForge.EVENT_BUS.register(this);
            FMLJavaModLoadingContext.get().getModEventBus().register(this);
        }

        @SubscribeEvent
        public void registerParticleType(RegistryEvent.Register<ParticleType<?>> event) {
            event.getRegistry().register(scarecrowParticle.setRegistryName("scarecrow_active_particle"));
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public void registerParticle(ParticleFactoryRegisterEvent event) {
            Minecraft.getInstance().particles.registerFactory(scarecrowParticle, ScarecrowParticleFactory::new);
        }
    }
}
