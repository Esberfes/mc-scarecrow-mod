package mc.scarecrow.common.entity;

import com.mojang.authlib.GameProfile;
import mc.scarecrow.common.init.CommonRegistryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

public class ScarecrowPlayerEntity extends FakePlayer {
    private ServerWorld world;
    private GameProfile profile;
    private Vector3d positionVec;
    private BlockPos position;

    private ScarecrowPlayerEntity(ServerWorld world, GameProfile profile) {
        super(world, profile);
        this.world = world;
        this.profile = profile;
    }

    private static List<ScarecrowPlayerEntity> playerEntityList = new ArrayList<>();

    public static synchronized ScarecrowPlayerEntity create(ServerWorld world, BlockPos pos, UUID uuid) {
        GameProfile profile = uuid == null ? new GameProfile(UUID.randomUUID(), UUID.randomUUID().toString()) : new GameProfile(uuid, uuid.toString());

        ScarecrowPlayerEntity player = new ScarecrowPlayerEntity(world, profile);
        player.connection = new FakeNetHandler(player);
        player.setState(pos);
        player.preventEntitySpawning = false;
        world.getPlayers().add(player);
        player.setGameType(GameType.SURVIVAL);

        return player;
    }

    public static synchronized void remove(ScarecrowPlayerEntity entity, ServerWorld world) {
        if (entity != null) {
            world.removePlayer(entity);
            playerEntityList.remove(entity);
        }
    }

    public static synchronized void removeAll(ServerWorld world) {
        playerEntityList.forEach(p -> {
            if (p != null) world.removePlayer(p);
        });

        playerEntityList.clear();
    }

    private void setState(BlockPos position) {
        setRawPosition(position.getX(), position.getY(), position.getZ());

        rotationYaw = 0.0f;
        rotationPitch = 0.0f;
        this.positionVec = new Vector3d(position.getX(), position.getY(), position.getZ());
        this.setPosition(position.getX(), position.getY(), position.getZ());
        this.position = new BlockPos(position.getX(), position.getY(), position.getZ());
        inventory.clear();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public float getHealth() {
        return 10F;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    public Vector3d getPositionVec() {
        return this.positionVec;
    }

    @Override
    public BlockPos getPosition() {
        return position;
    }

    public GameProfile getProfile() {
        return profile;
    }

    @Override
    public EntityType<?> getType() {
        return CommonRegistryHandler.FAKE_PLAYER.get();
    }

    @Override
    public float getEyeHeight(Pose pose) {
        return 0;
    }

    @Override
    public float getStandingEyeHeight(Pose pose, EntitySize size) {
        return 0;
    }

    //region Code which depends on the connection
    @Override
    public OptionalInt openContainer(INamedContainerProvider prover) {
        return OptionalInt.empty();
    }

    @Override
    public void sendEnterCombat() {
    }

    @Override
    public void sendEndCombat() {
    }

    @Override
    public boolean startRiding(Entity entityIn, boolean force) {
        return false;
    }

    @Override
    public void stopRiding() {
    }

    @Override
    public void openSignEditor(SignTileEntity signTile) {
    }

    @Override
    public void openHorseInventory(AbstractHorseEntity horse, IInventory inventory) {
    }

    @Override
    public void openBook(ItemStack stack, Hand hand) {
    }

    @Override
    public void closeScreen() {
    }

    @Override
    public void updateHeldItem() {
    }

    @Override
    protected void onNewPotionEffect(EffectInstance id) {
    }

    @Override
    protected void onChangedPotionEffect(EffectInstance id, boolean apply) {
    }

    @Override
    protected void onFinishedPotionEffect(EffectInstance effect) {
    }

//endregion
}