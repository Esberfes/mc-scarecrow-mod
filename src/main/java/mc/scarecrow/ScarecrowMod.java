package mc.scarecrow;

import mc.scarecrow.common.init.RegistryHandler;
import mc.scarecrow.constant.ScarecrowModConstants;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ScarecrowModConstants.MOD_IDENTIFIER)
public class ScarecrowMod {

    private static final Logger LOGGER = LogManager.getLogger();

    public ScarecrowMod() {
        LOGGER.debug("Initializing Registry");
        RegistryHandler.init();
        LOGGER.debug("Finish Registry");
    }
}
