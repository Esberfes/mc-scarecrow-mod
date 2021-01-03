package mc.scarecrow.common.block;

import mc.scarecrow.common.capability.ScarecrowTileCapabilities;
import mc.scarecrow.common.entity.FakePlayerEntity;
import mc.scarecrow.common.init.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

import static mc.scarecrow.constant.ScarecrowBlockConstants.INVENTORY_SIZE;

@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class ScarecrowTile extends LockableLootTileEntity implements IChestLid, ITickableTileEntity, Runnable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static String NTB_ACTIVE = "NTB_ACTIVE";
    private static String NTB_CURRENT_BURN_TIME = "NTB_BURN_TIME";
    private static String NTB_TOTAL_BURN_TIME = "NTB_TOTAL_BURN_TIME";

    private NonNullList<ItemStack> chestContents;
    protected float lidAngle;
    protected float prevLidAngle;
    protected int numPlayersUsing;
    private int ticksSinceSync;

    private int fuelTick;
    private int totalBurnTime;
    private int currentBurningTime;
    private final AtomicBoolean isActive;
    private final AtomicBoolean dataChange;
    private final AtomicBoolean updateInProgress;

    private FakePlayerEntity fakePlayerEntity;
    private PlayerEntity owner;

    public ScarecrowTile() {
        super(RegistryHandler.scarecrowTileBlock.get());
        this.chestContents = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        this.dataChange = new AtomicBoolean();
        this.dataChange.set(false);
        this.updateInProgress = new AtomicBoolean();
        this.updateInProgress.set(false);
        this.isActive = new AtomicBoolean();
        this.isActive.set(false);
    }

    @Override
    public int getSizeInventory() {
        return this.getItems().size();
    }

    public FakePlayerEntity getFakePlayerEntity() {
        return fakePlayerEntity;
    }

    public void setFakePlayerEntity(FakePlayerEntity fakePlayerEntity) {
        this.fakePlayerEntity = fakePlayerEntity;
    }

    public PlayerEntity getOwner() {
        return owner;
    }

    public void setOwner(PlayerEntity owner) {
        this.owner = owner;
    }

    @Override
    public void tick() {
        ++this.fuelTick;
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();
        ++this.ticksSinceSync;
        this.numPlayersUsing = getNumberOfPlayersUsing(this.world, this, this.ticksSinceSync, i, j, k, this.numPlayersUsing);
        this.prevLidAngle = this.lidAngle;
        float f = 0.1F;

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            this.playSound(SoundEvents.BLOCK_CHEST_OPEN);
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float f1 = this.lidAngle;
            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            float f2 = 0.5F;
            if (this.lidAngle < 0.5F && f1 >= 0.5F) {
                this.playSound(SoundEvents.BLOCK_CHEST_CLOSE);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }

        if (!world.isRemote() && fuelTick % 10 == 0 && !this.updateInProgress.get()) {
            updateInProgress.set(true);
            ((ServerWorld) world).getServer().deferTask(this::run);
        }
    }

    public static int getNumberOfPlayersUsing(World worldIn, LockableTileEntity lockableTileEntity, int ticksSinceSync, int x, int y, int z, int numPlayersUsing) {
        if (!worldIn.isRemote && numPlayersUsing != 0 && (ticksSinceSync + x + y + z) % 200 == 0) {
            numPlayersUsing = getNumberOfPlayersUsing(worldIn, lockableTileEntity, x, y, z);
        }

        return numPlayersUsing;
    }

    public static int getNumberOfPlayersUsing(World world, LockableTileEntity lockableTileEntity, int x, int y, int z) {
        int i = 0;

        for (PlayerEntity playerentity : world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB((double) ((float) x - 5.0F), (double) ((float) y - 5.0F), (double) ((float) z - 5.0F), (double) ((float) (x + 1) + 5.0F), (double) ((float) (y + 1) + 5.0F), (double) ((float) (z + 1) + 5.0F)))) {
            if (playerentity.openContainer instanceof ScarecrowContainer) {
                ++i;
            }
        }

        return i;
    }

    private void playSound(SoundEvent soundIn) {
        double d0 = (double) this.pos.getX() + 0.5D;
        double d1 = (double) this.pos.getY() + 0.5D;
        double d2 = (double) this.pos.getZ() + 0.5D;

        this.world.playSound(null, d0, d1, d2, soundIn, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public void onLoad() {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getLidAngle(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return chestContents;
    }

    @Override
    public void setItems(NonNullList<ItemStack> itemsIn) {
        this.chestContents = NonNullList.<ItemStack>withSize(INVENTORY_SIZE, ItemStack.EMPTY);

        for (int i = 0; i < itemsIn.size(); i++) {
            if (i < this.chestContents.size()) {
                this.getItems().set(i, itemsIn.get(i));
            }
        }
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new StringTextComponent("Scarecrow");
    }

    @Override
    public Container createMenu(int id, PlayerInventory player, PlayerEntity entity) {
        ScarecrowContainer scarecrowContainer = new ScarecrowContainer(
                id,
                world,
                getPos(),
                player,
                entity
        );

        return scarecrowContainer;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return null;
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    protected void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();

        if (block instanceof ScarecrowBlock) {
            this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, block);
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        if (this.dataChange.get()) {
            CompoundNBT compoundNBT = writeTag(new CompoundNBT());
            this.dataChange.set(false);

            return new SUpdateTileEntityPacket(getPos(), 0, compoundNBT);
        }

        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        try {
            handleTag(pkt.getNbtCompound());

        } catch (Throwable e) {
            LOGGER.error(e);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeTag(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        handleTag(tag);
    }

    private void handleTag(CompoundNBT tag) {
        try {
            this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

            if (!this.checkLootAndRead(tag))
                ItemStackHelper.loadAllItems(tag, this.chestContents);

            if (tag.contains(NTB_ACTIVE))
                this.isActive.set(tag.getBoolean(NTB_ACTIVE));

            if (tag.contains(NTB_CURRENT_BURN_TIME))
                this.currentBurningTime = tag.getInt(NTB_CURRENT_BURN_TIME);

            if (tag.contains(NTB_TOTAL_BURN_TIME))
                this.totalBurnTime = tag.getInt(NTB_TOTAL_BURN_TIME);

        } catch (Throwable e) {
            LOGGER.error(e);
        }
    }

    private CompoundNBT writeTag(CompoundNBT tag) {
        if (!this.checkLootAndWrite(tag))
            ItemStackHelper.saveAllItems(tag, this.chestContents);

        tag.putInt(NTB_CURRENT_BURN_TIME, this.currentBurningTime);
        tag.putInt(NTB_TOTAL_BURN_TIME, this.totalBurnTime);
        tag.putBoolean(NTB_ACTIVE, this.isActive.get());

        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        handleTag(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        return writeTag(compound);
    }

    public final void updateBlock() {
        if (this.world == null || this.world.isRemote)
            return;

        this.dataChange.set(true);
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
    }

    @Override
    public void remove() {
        super.remove();
        unloadAll();
    }

    public void unloadAll() {
        this.world.getCapability(ScarecrowTileCapabilities.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.world.getChunk(this.pos).getPos();
            tracker.remove(new ChunkPos(pos.x, pos.z), this.pos);
            if (owner != null)
                tracker.remove(owner.getGameProfile().getId(), this.pos);
        });

        if (this.fakePlayerEntity != null) {
            this.fakePlayerEntity.getServerWorld().getPlayers().remove(fakePlayerEntity);
        }

        this.updateBlock();
    }

    public void loadAll(ServerPlayerEntity serverPlayerEntity) {
        this.world.getCapability(ScarecrowTileCapabilities.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.world.getChunk(this.pos).getPos();
            tracker.add(new ChunkPos(pos.x, pos.z), this.pos);
            tracker.add(serverPlayerEntity.getGameProfile().getId(), this.pos);
        });

        this.updateBlock();
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void setActive(boolean active) {
        isActive.set(active);
    }

    public int getTotalBurnTime() {
        return totalBurnTime;
    }

    public int getCurrentBurningTime() {
        return currentBurningTime;
    }

    @Override
    public void run() {
        try {
            // Si no le queda combustible se intenta recargar, charcoal tiene 1600
            if (currentBurningTime <= 0 && chestContents.size() > 0) {
                ItemStack itemStack = chestContents.stream().findFirst().get();
                int count = itemStack.getCount();
                if (count > 0) {
                    ItemStack refuel = itemStack.split(1);
                    currentBurningTime = ForgeHooks.getBurnTime(refuel);
                }
            }

            totalBurnTime = currentBurningTime;

            for (ItemStack itemStack : chestContents)
                totalBurnTime += ForgeHooks.getBurnTime(itemStack) * itemStack.getCount();

            if (currentBurningTime > 0) {
                currentBurningTime -= fuelTick;
                // Aseguramos numeros positivos
                currentBurningTime = Math.max(currentBurningTime, 0);
                fuelTick = 0;
            }

            isActive.set(currentBurningTime > 0);

            this.updateBlock();
        } catch (Throwable e) {
            LOGGER.error(e);
        } finally {
            updateInProgress.set(false);
        }
    }

    public void toggle(int xOffset, int zOffset) {

    }
}
