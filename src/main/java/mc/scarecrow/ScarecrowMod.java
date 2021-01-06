package mc.scarecrow;

import mc.scarecrow.common.init.RegistryHandler;
import mc.scarecrow.common.network.ClientProxy;
import mc.scarecrow.common.network.IProxy;
import mc.scarecrow.common.network.ServerProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod(MOD_IDENTIFIER)
public class ScarecrowMod {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    /*@ObjectHolder(MOD_IDENTIFIER + ":scarecrow_block")
    public static ContainerType<ScarecrowContainer> TYPE = null;*/

    public ScarecrowMod() {
        //noinspection InstantiationOfUtilityClass
        MinecraftForge.EVENT_BUS.register(new RegistryHandler());
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.debug("Initializing registry");
        RegistryHandler.init();
        LOGGER.debug("Finishing registry");
    }
}
