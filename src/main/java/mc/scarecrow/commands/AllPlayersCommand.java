package mc.scarecrow.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AllPlayersCommand implements Command<CommandSource> {
    private static final AllPlayersCommand cmd = new AllPlayersCommand();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("allPlayers")
                .requires(x -> x.hasPermissionLevel(0))
                .executes(cmd);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        Long delay = 0L;

        for (ServerPlayerEntity playerEntity : context.getSource().asPlayer().getServerWorld().getPlayers()) {
            String playerInfo = playerEntity.getDisplayName().getStringTruncated(10) + " - " + "x: " + playerEntity.getPosX() + " y: " +
                    playerEntity.getPosY() + " z: " + playerEntity.getPosZ();

            executorService.schedule(() -> context.getSource().sendFeedback(new StringTextComponent(playerInfo), true), ++delay, TimeUnit.SECONDS);
        }

        return Command.SINGLE_SUCCESS;
    }
}
