package mc.scarecrow.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AllEntitiesInScarecrowCommand implements Command<CommandSource> {
    private static final AllEntitiesInScarecrowCommand cmd = new AllEntitiesInScarecrowCommand();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("scarecrow")
                .requires(x -> x.hasPermissionLevel(0))
                .executes(cmd);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        long delay = 0L;


        for (Entity entity : context.getSource().asPlayer().getServerWorld().getEntities().collect(Collectors.toList())) {
            String a = entity.getDisplayName().getStringTruncated(10) + " - "
                    + " x: " + entity.getPosX()
                    + " y: " + entity.getPosY()
                    + " z: " + entity.getPosZ();

            if (entity instanceof MonsterEntity)
                executorService.schedule(() -> context.getSource().sendFeedback(new StringTextComponent(a), true), (delay += 100), TimeUnit.MILLISECONDS);
        }

        return Command.SINGLE_SUCCESS;
    }
}
