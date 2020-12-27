package mc.scarecrow.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.entity.FakePlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

            FakePlayerEntity entity = new FakePlayerEntity.Builder()
                    .server(serverPlayerEntity.getServer())
                    .world(serverPlayerEntity.getServerWorld())
                    .build();

            entity.setPositionAndUpdate(serverPlayerEntity.getPosX(), serverPlayerEntity.getPosY(), serverPlayerEntity.getPosZ());

            serverPlayerEntity.sendStatusMessage(new StringTextComponent("FakePlayer created: " + entity.getProfile().getName()), true);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
        }

        return Command.SINGLE_SUCCESS;
    }
}
