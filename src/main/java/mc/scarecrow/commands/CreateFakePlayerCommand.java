package mc.scarecrow.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mc.scarecrow.entity.FakePlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CreateFakePlayerCommand implements Command<CommandSource> {

    private static final CreateFakePlayerCommand cmd = new CreateFakePlayerCommand();
    private static List<WeakReference<ServerPlayerEntity>> players = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("fakePlayer")
                .requires(x -> x.hasPermissionLevel(0))
                .executes(cmd);
    }

    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity serverPlayerEntity = context.getSource().asPlayer();

            LOGGER.warn("World is remote: " + serverPlayerEntity.world.isRemote);

            serverPlayerEntity.getServer().deferTask(() -> {
                FakePlayerEntity entity = FakePlayerEntity.create(serverPlayerEntity.getServerWorld(), serverPlayerEntity.getPosition(), null);
                serverPlayerEntity.sendStatusMessage(new StringTextComponent("FakePlayer created: " + entity.getProfile().getName()), true);
            });

        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
        }

        return Command.SINGLE_SUCCESS;
    }
}
