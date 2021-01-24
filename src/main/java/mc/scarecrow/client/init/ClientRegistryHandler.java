package mc.scarecrow.client.init;

import mc.scarecrow.client.particle.ScarecrowParticleFactory;
import mc.scarecrow.client.renderer.ScarecrowTileRenderer;
import mc.scarecrow.client.screen.ScarecrowScreen;
import mc.scarecrow.lib.register.LibAutoRegister;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistryHandler {

    public static BasicParticleType scarecrowParticle = new BasicParticleType(true);

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        try {
            // Register on client the screen for the container
            ScreenManager.registerFactory(LibAutoRegister.CONTAINERS.get("scarecrow"),
                    (ScreenManager.IScreenFactory<Container, ContainerScreen<Container>>) ScarecrowScreen::new);

            // Register on client the scarecrow tile renderer
            ClientRegistry.bindTileEntityRenderer((TileEntityType<? extends TileEntity>) LibAutoRegister.TILE_ENTITIES.get("scarecrow"),
                    o -> new ScarecrowTileRenderer(o, LibAutoRegister.BLOCKS.get("scarecrow")));
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @SubscribeEvent
    public static void registerParticleType(RegistryEvent.Register<ParticleType<?>> event) {
        event.getRegistry().register(scarecrowParticle.setRegistryName("scarecrow_active_particle"));
    }

    @SubscribeEvent
    public static void registerParticle(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(scarecrowParticle, ScarecrowParticleFactory::new);
    }
}
