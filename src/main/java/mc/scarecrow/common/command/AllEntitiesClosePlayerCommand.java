package mc.scarecrow.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AllEntitiesClosePlayerCommand implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final AllEntitiesClosePlayerCommand cmd = new AllEntitiesClosePlayerCommand();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("allEntitiesClose")
                .requires(x -> x.hasPermissionLevel(0))
                .executes(cmd);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        try {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            long delay = 0L;

            for (ServerPlayerEntity playerEntity : context.getSource().asPlayer().getServerWorld().getPlayers()) {
                for (Entity entity : context.getSource().asPlayer().getServerWorld().getEntities().filter(e -> e.getDistance(playerEntity) <= 128.0D).collect(Collectors.toList())) {
                    String a = playerEntity.getName().getString() + " - " + playerEntity.getPosition().getCoordinatesAsString() + ": "
                            + entity.getDisplayName().getStringTruncated(10) + " - "
                            + " x: " + entity.getPosX()
                            + " y: " + entity.getPosY()
                            + " z: " + entity.getPosZ();

                    if (entity instanceof MonsterEntity)
                        executorService.schedule(() -> context.getSource().sendFeedback(new StringTextComponent(a), true), (delay += 100), TimeUnit.MILLISECONDS);
                }
            }

        } catch (Throwable e) {
            LOGGER.error(e);
        }


        return Command.SINGLE_SUCCESS;
    }
}
