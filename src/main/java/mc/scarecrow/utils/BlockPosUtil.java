package mc.scarecrow.utils;

import com.sun.javafx.geom.Vec3d;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;



public abstract class BlockPosUtil {
    /**
     * Max depth of the floor check to avoid endless void searching (Stackoverflow).
     */
    private static final int MAX_DEPTH = 50;
    public static final int UP_DOWN_RANGE = 3;
    /**
     * Amount of string required to try to calculate a blockpos.
     */
    private static final int BLOCKPOS_LENGTH = 3;
    public static final int MAX_TRIES = 20;
    private BlockPosUtil() {
        //Hide default constructor.
    }

    /**
     * Writes a Chunk Coordinate to an NBT compound, with a specific tag name.
     *
     * @param compound Compound to write to.
     * @param name     Name of the tag.
     * @param pos      Coordinates to write to NBT.
     * @return the resulting compound.
     */
    public static CompoundNBT write( final CompoundNBT compound, final String name,  final BlockPos pos) {
         final CompoundNBT coordsCompound = new CompoundNBT();
        coordsCompound.putInt("x", pos.getX());
        coordsCompound.putInt("y", pos.getY());
        coordsCompound.putInt("z", pos.getZ());
        compound.put(name, coordsCompound);
        return compound;
    }

    /**
     * Searches a random direction.
     *
     * @param random a random object.
     * @return a tuple of two directions.
     */
    public static Tuple<Direction, Direction> getRandomDirectionTuple(final Random random) {
        return new Tuple<>(Direction.getRandomDirection(random), Direction.getRandomDirection(random));
    }

    /**
     * Gets a random position within a certain range for wandering around.
     *
     * @param world           the world.
     * @param currentPosition the current position.
     * @param def             the default position if none was found.
     * @param minDist         the minimum distance of the pos.
     * @param maxDist         the maximum distance.
     * @return the BlockPos.
     */
    public static BlockPos getRandomPosition(final World world, final BlockPos currentPosition, final BlockPos def, final int minDist, final int maxDist) {
        final Random random = world.rand;

        int tries = 0;
        BlockPos pos = null;
        while (pos == null
                || !WorldUtils.isEntityBlockLoaded(world, pos)
                || world.getBlockState(pos).getMaterial().isLiquid()
                || !world.getBlockState(pos.down()).getMaterial().isSolid()
                || (!world.isAirBlock(pos) || !world.isAirBlock(pos.up()))) {
            final Tuple<Direction, Direction> direction = getRandomDirectionTuple(random);
            pos =
                    new BlockPos(currentPosition)
                            .offset(direction.getA(), random.nextInt(maxDist) + minDist)
                            .offset(direction.getB(), random.nextInt(maxDist) + minDist)
                            .up(random.nextInt(UP_DOWN_RANGE))
                            .down(random.nextInt(UP_DOWN_RANGE));

            if (tries >= MAX_TRIES) {
                return def;
            }

            tries++;
        }

        return pos;
    }

    /**
     * Reads Chunk Coordinates from an NBT Compound with a specific tag name.
     *
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return Chunk coordinates read from the compound.
     */

    public static BlockPos read( final CompoundNBT compound, final String name) {
        final CompoundNBT coordsCompound = compound.getCompound(name);
        final int x = coordsCompound.getInt("x");
        final int y = coordsCompound.getInt("y");
        final int z = coordsCompound.getInt("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Write a compound with chunk coordinate to a tag list.
     *
     * @param tagList Tag list to write compound with chunk coordinates to.
     * @param pos     Coordinate to write to the tag list.
     */
    public static void writeToListNBT( final ListNBT tagList,  final BlockPos pos) {
         final CompoundNBT coordsCompound = new CompoundNBT();
        coordsCompound.putInt("x", pos.getX());
        coordsCompound.putInt("y", pos.getY());
        coordsCompound.putInt("z", pos.getZ());
        tagList.add(coordsCompound);
    }

    /**
     * Reads a Chunk Coordinate from a tag list.
     *
     * @param tagList Tag list to read compound with chunk coordinate from.
     * @param index   Index in the tag list where the required chunk coordinate is.
     * @return Chunk coordinate read from the tag list.
     */

    public static BlockPos readFromListNBT( final ListNBT tagList, final int index) {
        final CompoundNBT coordsCompound = tagList.getCompound(index);
        final int x = coordsCompound.getInt("x");
        final int y = coordsCompound.getInt("y");
        final int z = coordsCompound.getInt("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Try to parse a blockPos of an input string.
     *
     * @param inputText the string to parse.
     * @return the blockPos if able to.
     */
    @Nullable
    public static BlockPos getBlockPosOfString( final String inputText) {
        final String[] strings = inputText.split(" ");

        if (strings.length == BLOCKPOS_LENGTH) {
            try {
                final int x = Integer.parseInt(strings[0]);
                final int y = Integer.parseInt(strings[1]);
                final int z = Integer.parseInt(strings[2]);
                return new BlockPos(x, y, z);
            } catch (final NumberFormatException e) {
                /*
                 * Empty for a purpose.
                 */
            }
        }
        return null;
    }

    /**
     * Returns a string representation of the block position for use in GUIs and chats.
     *
     * @param position The position of the string to be returned
     * @return The string representation of the block position
     */

    public static String getString( final BlockPos position) {
        return "{x=" + position.getX() + ", y=" + position.getY() + ", z=" + position.getZ() + "}";
    }

    /**
     * this checks that you are not in liquid.  Will check for all liquids, even those from other mods before TP
     *
     * @param sender   uses the player to get the world
     * @param blockPos for the current block LOC
     * @return isSafe true=safe false=water or lava
     */
    public static boolean isPositionSafe( final World sender, final BlockPos blockPos) {
        return !(sender.getBlockState(blockPos).getBlock() instanceof AirBlock)
                && !sender.getBlockState(blockPos).getMaterial().isLiquid()
                && !sender.getBlockState(blockPos.down()).getMaterial().isLiquid()
                && sender.getWorldBorder().contains(blockPos);
    }

    /**
     * this checks that you are not in the air or underground. If so it will look up and down for a good landing spot before TP.
     *
     * @param blockPos for the current block LOC.
     * @param world    the world to search in.
     * @return blockPos to be used for the TP.
     */
    public static BlockPos findLand(final BlockPos blockPos, final World world) {
        int top = blockPos.getY();
        int bot = 0;
        int mid = blockPos.getY();

        BlockPos foundland = null;
        BlockPos tempPos = blockPos;
        //We are doing a binary search to limit the amount of checks (usually at most 9 this way)
        while (top >= bot) {
            tempPos = new BlockPos(tempPos.getX(), mid, tempPos.getZ());
            final Block block = world.getBlockState(tempPos).getBlock();
            if (block instanceof AirBlock && world.canBlockSeeSky(tempPos)) {
                top = mid - 1;
                foundland = tempPos;
            } else {
                bot = mid + 1;
                foundland = tempPos;
            }
            mid = (bot + top) / 2;
        }

        if (world.getBlockState(tempPos).getMaterial().isSolid()) {
            return foundland.up();
        }

        return foundland;
    }

    /**
     * Returns the right height for the given position (ground block).
     *
     * @param position Current position of the entity.
     * @param world    the world object.
     * @return Ground level at (position.x, position.z).
     */
    public static double getValidHeight( final Vec3d position,  final World world) {
        double returnHeight = position.y;
        if (position.y < 0) {
            returnHeight = 0;
        }

        while (returnHeight >= 1 && world.isAirBlock(new BlockPos(MathHelper.floor(position.x),
                (int) returnHeight,
                MathHelper.floor(position.z)))) {
            returnHeight -= 1.0D;
        }

        while (!world.isAirBlock(
                new BlockPos(MathHelper.floor(position.x), (int) returnHeight, MathHelper.floor(position.z)))) {
            returnHeight += 1.0D;
        }
        return returnHeight;
    }

    /**
     * Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return squared distance.
     */
    public static long getDistanceSquared( final BlockPos block1,  final BlockPos block2) {
        final long xDiff = (long) block1.getX() - block2.getX();
        final long yDiff = (long) block1.getY() - block2.getY();
        final long zDiff = (long) block1.getZ() - block2.getZ();

        final long result = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
        if (result < 0) {
            throw new IllegalStateException("max-sqrt is to high! Failure to catch overflow with "
                    + xDiff + " | " + yDiff + " | " + zDiff);
        }
        return result;
    }

    /**
     * X+Z Distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return X+Z distance
     */
    public static long getDistance2D( final BlockPos block1,  final BlockPos block2) {
        final long xDiff = Math.abs((long) block1.getX() - block2.getX());
        final long zDiff = Math.abs((long) block1.getZ() - block2.getZ());

        return Math.abs(xDiff + zDiff);
    }

    /**
     * Maximum of x/z distance between two blocks.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return X or Z distance
     */
    public static int getMaxDistance2D( final BlockPos block1,  final BlockPos block2) {
        final int xDif = Math.abs(block1.getX() - block2.getX());
        final int zDif = Math.abs(block1.getZ() - block2.getZ());

        return Math.max(xDif, zDif);
    }

    /**
     * Gets the actual distance in blocks between two positions
     *
     * @param pos1 Blockpos 1
     * @param pos2 Blockpos 2
     * @return distance in blocks.
     */
    public static double getDistance(final BlockPos pos1, final BlockPos pos2) {
        final long xDiff = pos1.getX() - pos2.getX();
        final long yDiff = pos1.getY() - pos2.getY();
        final long zDiff = pos1.getZ() - pos2.getZ();

        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    /**
     * 2D Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return 2D squared distance.
     */
    public static long getDistanceSquared2D( final BlockPos block1,  final BlockPos block2) {
        final long xDiff = (long) block1.getX() - block2.getX();
        final long zDiff = (long) block1.getZ() - block2.getZ();

        final long result = xDiff * xDiff + zDiff * zDiff;
        if (result < 0) {
            throw new IllegalStateException("max-sqrt is to high! Failure to catch overflow with "
                    + xDiff + " | " + zDiff);
        }
        return result;
    }

    /**
     * Returns the tile entity at a specific chunk coordinate.
     *
     * @param world World the tile entity is in.
     * @param pos   Coordinates of the tile entity.
     * @return Tile entity at the given coordinates.
     */
    public static TileEntity getTileEntity( final World world,  final BlockPos pos) {
        return world.getTileEntity(pos);
    }

    /**
     * Returns the block at a specific chunk coordinate.
     *
     * @param world  World the block is in.
     * @param coords Coordinates of the block.
     * @return Block at the given coordinates.
     */
    public static Block getBlock( final World world,  final BlockPos coords) {
        return world.getBlockState(coords).getBlock();
    }

    /**
     * Returns the metadata of a block at a specific chunk coordinate.
     *
     * @param world  World the block is in.
     * @param coords Coordinates of the block.
     * @return Metadata of the block at the given coordinates.
     */
    public static BlockState getBlockState( final World world,  final BlockPos coords) {
        return world.getBlockState(coords);
    }

    /**
     * Sets a block in the world, with specific metadata and flags.
     *
     * @param worldIn World the block needs to be set in.
     * @param coords  Coordinate to place block.
     * @param state   BlockState to be placed.
     * @param flag    Flag to set.
     * @return True if block is placed, otherwise false.
     */
    public static boolean setBlock( final World worldIn,  final BlockPos coords, final BlockState state, final int flag) {
        return worldIn.setBlockState(coords, state, flag);
    }

    /**
     * {@link EntityUtils#tryMoveLivingToXYZ(net.minecraft.entity.MobEntity, int, int, int)}.
     *
     * @param living      A living entity.
     * @param destination chunk coordinates to check moving to.
     * @return True when XYZ is found, an set moving to, otherwise false.
     */
    public static boolean tryMoveLivingToXYZ( final MobEntity living,  final BlockPos destination) {
        return EntityUtils.tryMoveLivingToXYZ(living, destination.getX(), destination.getY(), destination.getZ());
    }

    /**
     * Create a method for using a {@link BlockPos} when using {@link net.minecraft.util.math.BlockPos.Mutable#setPos(int, int, int)}.
     *
     * @param pos    {@link net.minecraft.util.math.BlockPos.Mutable}.
     * @param newPos The new position to set.
     */
    public static void set( final BlockPos.Mutable pos,  final BlockPos newPos) {
        pos.setPos(newPos.getX(), newPos.getY(), newPos.getZ());
    }

    /**
     * Returns whether a chunk coordinate is equals to (x, y, z).
     *
     * @param coords Chunk Coordinate    (point 1).
     * @param x      x-coordinate        (point 2).
     * @param y      y-coordinate        (point 2).
     * @param z      z-coordinate        (point 2).
     * @return True when coordinates are equal, otherwise false.
     */
    public static boolean isEqual( final BlockPos coords, final int x, final int y, final int z) {
        return coords.getX() == x && coords.getY() == y && coords.getZ() == z;
    }

    /**
     * Returns the Chunk Coordinate created from an entity.
     *
     * @param entity Entity to create chunk coordinates from.
     * @return Chunk Coordinates created from the entity.
     */

    public static BlockPos fromEntity( final Entity entity) {
        return new BlockPos(MathHelper.floor(entity.getPosX()), MathHelper.floor(entity.getPosY()), MathHelper.floor(entity.getPosZ()));
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @param world    the world the position is in.
     * @return returns BlockPos position with air above.
     */

    public static BlockPos getFloor( final BlockPos position,  final World world) {
        final BlockPos floor = getFloor(new BlockPos.Mutable(position.getX(), position.getY(), position.getZ()), 0, world);
        if (floor == null) {
            return position;
        }
        return floor;
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @param depth    the iteration depth.
     * @param world    the world the position is in.
     * @return returns BlockPos position with air above.
     */
    @Nullable
    public static BlockPos getFloor( final BlockPos.Mutable position, final int depth,  final World world) {
        if (depth > MAX_DEPTH) {
            return null;
        }
        //If the position is floating in Air go downwards
        if (!EntityUtils.solidOrLiquid(world, position)) {
            return getFloor(position.setPos(position.getX(), position.getY() - 1, position.getZ()), depth + 1, world);
        }
        //If there is no air above the block go upwards
        if (!EntityUtils.solidOrLiquid(world, position.setPos(position.getX(), position.getY() + 1, position.getZ())) &&
                !EntityUtils.solidOrLiquid(world, position.setPos(position.getX(), position.getY() + 2, position.getZ()))) {
            return position.toImmutable();
        }
        return getFloor(position.setPos(position.getX(), position.getY() + 1, position.getZ()), depth + 1, world);
    }

    /**
     * Calculate in which direction a pos is facing.
     *
     * @param pos      the pos.
     * @param neighbor the block its facing.
     * @return the directions its facing.
     */
    public static Direction getFacing(final BlockPos pos, final BlockPos neighbor) {
        final BlockPos vector = neighbor.subtract(pos);
        return Direction.getFacingFromVector(vector.getX(), vector.getY(), -vector.getZ());
    }

    /**
     * Calculate in which direction a pos is facing. Ignoring y.
     *
     * @param pos      the pos.
     * @param neighbor the block its facing.
     * @return the directions its facing.
     */
    public static Direction getXZFacing(final BlockPos pos, final BlockPos neighbor) {
        final BlockPos vector = neighbor.subtract(pos);
        return Direction.getFacingFromVector(vector.getX(), 0, vector.getZ());
    }
    /**
     * Returns the first air position near the given start. Advances vertically first then horizontally
     *
     * @param start     start position
     * @param vRange    vertical search range
     * @param hRange    horizontal search range
     * @param predicate check predicate for the right block
     * @return position or null
     */
    public static BlockPos findAround(final IBlockReader world, final BlockPos start, final int vRange, final int hRange, final Predicate<BlockState> predicate) {
        if (vRange < 1 && hRange < 1) {
            return null;
        }

        BlockPos temp;
        int y = 0;
        int y_offset = 1;

        for (int i = 0; i < hRange + 2; i++) {
            for (int steps = 1; steps <= vRange; steps++) {
                // Start topleft of middle point
                temp = start.add(-steps, y, -steps);

                // X ->
                for (int x = 0; x <= steps; x++) {
                    temp = temp.add(1, 0, 0);
                    if (predicate.test(world.getBlockState(temp)) && predicate.test(world.getBlockState(temp.up()))) {
                        return temp;
                    }
                }

                // X
                // |
                // v
                for (int z = 0; z <= steps; z++) {
                    temp = temp.add(0, 0, 1);
                    if (predicate.test(world.getBlockState(temp)) && predicate.test(world.getBlockState(temp.up()))) {
                        return temp;
                    }
                }

                // < - X
                for (int x = 0; x <= steps; x++) {
                    temp = temp.add(-1, 0, 0);
                    if (predicate.test(world.getBlockState(temp)) && predicate.test(world.getBlockState(temp.up()))) {
                        return temp;
                    }
                }

                // ^
                // |
                // X
                for (int z = 0; z <= steps; z++) {
                    temp = temp.add(0, 0, -1);
                    if (predicate.test(world.getBlockState(temp)) && predicate.test(world.getBlockState(temp.up()))) {
                        return temp;
                    }
                }
            }

            y += y_offset;
            y_offset++;
            y_offset *= -1;
        }

        return null;
    }
}