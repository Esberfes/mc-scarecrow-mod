package mc.scarecrow.common.init.events;

import mc.scarecrow.common.block.ScarecrowContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.ScarecrowMod.PROXY;
import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContainersRegisterEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @ObjectHolder(MOD_IDENTIFIER + ":scarecrow_block")
    public static ContainerType<ScarecrowContainer> TYPE = null;

    @SubscribeEvent
    public static void onContainerRegistry(RegistryEvent.Register<ContainerType<?>> event) {
        IForgeRegistry<ContainerType<?>> r = event.getRegistry();
        r.register(IForgeContainerType.create((windowId, inv, data) -> {
                    BlockPos pos = data.readBlockPos();
                    return new ScarecrowContainer(windowId, PROXY.getPlayerWorld(), pos, inv, PROXY.getPlayerEntity());
                }
        ).setRegistryName("scarecrow_block"));
    }
}
