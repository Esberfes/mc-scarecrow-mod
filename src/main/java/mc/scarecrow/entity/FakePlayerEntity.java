package mc.scarecrow.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.Stat;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class FakePlayerEntity extends FakePlayer {

    private ServerWorld world;
    private GameProfile profile;

    private FakePlayerEntity(ServerWorld world, GameProfile profile) {
        super(world, profile);
        this.world = world;
        this.profile = profile;
    }

    public static class Builder {
        private FakePlayerEntity entity;
        private ServerWorld world;
        private GameProfile profile;
        private MinecraftServer server;

        public Builder() {

        }

        public Builder server(MinecraftServer server) {
            this.server = server;

            return this;
        }

        public Builder world(ServerWorld world) {
            this.world = world;

            return this;
        }

        public Builder profile(GameProfile profile) {
            this.profile = profile;

            return this;
        }

        public FakePlayerEntity build() {
            if(profile == null)
                profile = new GameProfile(UUID.randomUUID(), UUID.randomUUID().toString());

            if(server == null)
                throw new RuntimeException("Can not create a fake player without MinecraftServer object reference");

            if(world == null)
                throw new RuntimeException("Can not create a fake player without ServerWorld object reference");

            entity = new FakePlayerEntity(world, profile);

            entity.connection = new FakeServerPlayNetHandler(server, entity);

            world.addNewPlayer(entity);

            return entity;
        }
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public boolean isInvisibleToPlayer(PlayerEntity player) {
        return false;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public GameProfile getProfile() {
        return profile;
    }
}
