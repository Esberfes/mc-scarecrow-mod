package mc.scarecrow.init.events;

import com.mojang.brigadier.CommandDispatcher;
import mc.scarecrow.commands.AllEntitiesCommand;
import mc.scarecrow.commands.AllPlayersCommand;
import mc.scarecrow.commands.CreateFakePlayerCommand;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandsRegisterEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        LOGGER.debug("onRegisterCommand");
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        dispatcher.register(AllEntitiesCommand.register());
        dispatcher.register(CreateFakePlayerCommand.register());
        dispatcher.register(AllPlayersCommand.register());
    }
}
