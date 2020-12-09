package mc.scarecrow.mod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.mod.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod(MOD_IDENTIFIER)
public class ScarecrowMod {

    private static final Logger LOGGER = LogManager.getLogger();

    public ScarecrowMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {

    }
}
