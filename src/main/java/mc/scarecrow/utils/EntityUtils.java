package mc.scarecrow.utils;


import mc.scarecrow.entity.FakePlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class EntityUtils {
    /**
     * How many blocks the citizen needs to stand safe.
     */
    private static final int AIR_SPACE_ABOVE_TO_CHECK = 2;

    /**
     * Default range for moving to something until we stop.
     */
    private static final int DEFAULT_MOVE_RANGE = 3;
    private static final int TELEPORT_RANGE = 512;
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;
    private static final int SCAN_RADIUS = 5;

    /**
     * Private constructor to hide the implicit public one.
     */
    private EntityUtils() {

    }

    /**
     * Checks if a player is a fakePlayer and tries to get the owning player if possible.
     *
     * @param player the incoming player.
     * @param world  the world.
     * @return the PlayerEntity owner in the best case.
     */

    public static PlayerEntity getPlayerOfFakePlayer( final PlayerEntity player,  final World world) {
        if (player instanceof FakePlayer || player instanceof FakePlayerEntity) {
            final PlayerEntity tempPlayer = world.getPlayerByUuid(player.getUniqueID());
            if (tempPlayer != null) {
                return tempPlayer;
            }
        }
        return player;
    }

    /**
     * Returns the loaded Entity with the given UUID.
     *
     * @param world world the entity is in
     * @param id    the entity's UUID
     * @return the Entity
     */
    public static Entity getPlayerByUUID( final World world,  final UUID id) {
        return world.getPlayerByUuid(id);
    }

    /**
     * Returns a list of loaded entities whose id's match the ones provided.
     *
     * @param world the world the entities are in.
     * @param ids   List of Entity id's
     * @return list of Entity's
     */
    public static List<Entity> getEntitiesFromID( final World world,  final List<Integer> ids) {
        return ids.stream()
                .map(world::getEntityByID)
                .collect(Collectors.toList());
    }

    /**
     * Returns the new rotation degree calculated from the current and intended rotation up to a max.
     *
     * @param currentRotation  the current rotation the citizen has.
     * @param intendedRotation the wanted rotation he should have after applying this.
     * @param maxIncrement     the 'movement speed.
     * @return a rotation value he should move.
     */
    public static double updateRotation(final double currentRotation, final double intendedRotation, final double maxIncrement) {
        double wrappedAngle = MathHelper.wrapDegrees(intendedRotation - currentRotation);

        if (wrappedAngle > maxIncrement) {
            wrappedAngle = maxIncrement;
        }

        if (wrappedAngle < -maxIncrement) {
            wrappedAngle = -maxIncrement;
        }

        return currentRotation + wrappedAngle;
    }

    /**
     * Check for free space AIR_SPACE_ABOVE_TO_CHECK blocks high.
     * <p>
     * And ensure a solid ground
     *
     * @param world          the world to look in
     * @param groundPosition the position to maybe stand on
     * @return true if a suitable Place to walk to
     */
    public static boolean checkForFreeSpace( final World world,  final BlockPos groundPosition) {
        for (int i = 1; i < AIR_SPACE_ABOVE_TO_CHECK; i++) {
            if (solidOrLiquid(world, groundPosition.up(i)) || world.getBlockState(groundPosition.up(i)).getBlock().isIn(BlockTags.LEAVES)) {
                return false;
            }
        }
        return world.getBlockState(groundPosition).getMaterial().isSolid();
    }

    /**
     * Checks if a blockPos in a world is solid or liquid.
     * <p>
     * Useful to find a suitable Place to stand. (avoid these blocks to find one)
     *
     * @param world    the world to look in
     * @param blockPos the blocks position
     * @return true if solid or liquid
     */
    public static boolean solidOrLiquid( final World world,  final BlockPos blockPos) {
        final Material material = world.getBlockState(blockPos).getMaterial();
        return material.isSolid()
                || material.isLiquid();
    }

    /**
     * Get a safe spawnpoint near a location.
     *
     * @param world     the world he should spawn in.
     * @param nearPoint the point to search near.
     * @return The spawn position.
     */
    @Nullable
    public static BlockPos getSpawnPoint(final World world, final BlockPos nearPoint) {
        return Utils.scanForBlockNearPoint(
                world,
                nearPoint.down(),
                1,
                2,
                1,
                2,
                Blocks.AIR,
                Blocks.CAVE_AIR,
                Blocks.SNOW,
                Blocks.TALL_GRASS,
                Blocks.WATER);
    }

    /**
     * Sets the movement of the entity to specific point. Returns true if direction is set, otherwise false. {@link #tryMoveLivingToXYZ(MobEntity, int, int, int, double)}
     *
     * @param living Entity to move
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @return True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ( final MobEntity living, final int x, final int y, final int z) {
        return tryMoveLivingToXYZ(living, x, y, z, 1.0D);
    }

    /**
     * Sets the movement of the entity to specific point. Returns true if direction is set, otherwise false.
     *
     * @param living      Entity to move
     * @param x           x-coordinate
     * @param y           y-coordinate
     * @param z           z-coordinate
     * @param speedFactor Speedfactor to modify base speed with
     * @return True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ( final MobEntity living, final int x, final int y, final int z, final double speedFactor) {
        return living.getNavigator().tryMoveToXYZ(x, y, z, speedFactor);
    }

    /**
     * Checks if the target is flying
     *
     * @param target entity to check
     * @return true if flying or falling deeper
     */
    public static boolean isFlying(final LivingEntity target) {
        return target != null && (target.isAirBorne || !target.isOnGround()) && target.fallDistance <= 0.1f && target.world.isAirBlock(target.func_233644_dn_().get().down(2));
    }
}