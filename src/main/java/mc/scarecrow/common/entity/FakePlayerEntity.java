package mc.scarecrow.common.entity;

import com.mojang.authlib.GameProfile;
import mc.scarecrow.common.init.RegistryHandler;
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

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.UUID;

public class FakePlayerEntity extends FakePlayer {

    private ServerWorld world;
    private GameProfile profile;
    private Vector3d positionVec;
    private BlockPos position;

    private FakePlayerEntity(ServerWorld world, GameProfile profile) {
        super(world, profile);
        this.world = world;
        this.profile = profile;
    }

    private static final GameProfile DEFAULT_PROFILE = new GameProfile(
            UUID.fromString("0d0c4ca0-4ff1-11e4-916c-0800200c9a66"),
            "[Scarecrow]"
    );


    public static FakePlayerEntity create( ServerWorld world, BlockPos pos, UUID uuid) {
        GameProfile profile = uuid == null ?  new GameProfile(UUID.randomUUID(), UUID.randomUUID().toString()) : new GameProfile(uuid, uuid.toString());

        FakePlayerEntity player = new FakePlayerEntity(world, getProfile(profile));
        player.connection = new FakeNetHandler(player);
        player.setState(pos);
        player.preventEntitySpawning = false;
        world.getPlayers().add(player);
        player.setGameType(GameType.SURVIVAL);

        return player;
    }

    private static GameProfile getProfile(@Nullable GameProfile profile) {
        return profile != null && profile.isComplete() ? profile : DEFAULT_PROFILE;
    }

    private void setState(BlockPos position) {
        setRawPosition(position.getX(), position.getY() - 1, position.getZ());

        rotationYaw = 0.0f;
        rotationPitch = 0.0f;
        this.positionVec = new Vector3d(position.getX(), position.getY() - 1, position.getZ());
        this.setPosition(position.getX(), position.getY() - 1, position.getZ());
        this.position = new BlockPos(position.getX(), position.getY() - 1, position.getZ());
        inventory.clear();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return false;
    }

    @Override
    public float getHealth() {
        return 10F;
    }

    @Override
    public boolean isInvulnerable() {
        return false;
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
        return RegistryHandler.FAKE_PLAYER.get();
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
