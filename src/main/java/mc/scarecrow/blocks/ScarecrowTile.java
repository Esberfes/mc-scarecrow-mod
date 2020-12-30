package mc.scarecrow.blocks;

import mc.scarecrow.capabilities.ScarecrowTileCapabilities;
import mc.scarecrow.entity.FakePlayerEntity;
import mc.scarecrow.init.RegistryHandler;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

import static mc.scarecrow.constant.ScarecrowBlockConstants.INVENTORY_SIZE;

@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class ScarecrowTile extends LockableLootTileEntity implements IChestLid, ITickableTileEntity {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String NTB_FAKE_PLAYER_UUID = "NTB_FAKE_PLAYER_UUID";
    private static String NTB_OWNER_PLAYER_UUID = "NTB_OWNER_PLAYER_UUID";
    private static String NTB_CHUNK_POS = "NTB_CHUNK_POS";
    private NonNullList<ItemStack> chestContents;
    protected float lidAngle;
    protected float prevLidAngle;
    protected int numPlayersUsing;
    private int ticksSinceSync;
    private boolean dataChanged = true;

    private FakePlayerEntity fakePlayerEntity;
    private UUID fakePlayerUUID;
    private PlayerEntity owner;
    private UUID ownerUUID;

    public ScarecrowTile() {
        super(RegistryHandler.scarecrowTileBlock.get());
        this.chestContents = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
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
        if(!world.isRemote() && (ticksSinceSync % 100 == 0))  {
            if(owner != null)
                setCustomName(owner.getDisplayName());
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
    public void onLoad() { // Se ejecuta en el servidor
        if (world instanceof ServerWorld) {

        }
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
    protected Container createMenu(int id, PlayerInventory player) {
        return new ScarecrowContainer(RegistryHandler.scarecrowBlockContainer.get(), id, player, this);
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
        CompoundNBT compoundNBT = writeTag(new CompoundNBT());
        //Write your data into the nbtTag
        return new SUpdateTileEntityPacket(getPos(), 2, compoundNBT);
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

            if (!this.checkLootAndRead(tag)) {
                ItemStackHelper.loadAllItems(tag, this.chestContents);
            }

        } catch (Throwable e) {
            LOGGER.error(e);
        }
    }

    private CompoundNBT writeTag(CompoundNBT tag) {
        if (!this.checkLootAndWrite(tag))
            ItemStackHelper.saveAllItems(tag, this.chestContents);

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
        if(this.world == null || this.world.isRemote)
            return;
        this.dataChanged = true;
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
    }

    @Override
    public void remove() {
        super.remove();
        unloadAll();
    }

    public void unloadAll(){
        this.world.getCapability(ScarecrowTileCapabilities.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.world.getChunk(this.pos).getPos();
            tracker.remove(new ChunkPos(pos.x , pos.z), this.pos);
            if(owner != null)
                 tracker.remove(owner.getGameProfile().getId(), this.pos);
        });

        if (this.fakePlayerEntity != null) {
            this.fakePlayerEntity.getServerWorld().getPlayers().remove(fakePlayerEntity);
        }
    }

    public void loadAll(ServerPlayerEntity serverPlayerEntity){
        this.world.getCapability(ScarecrowTileCapabilities.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.world.getChunk(this.pos).getPos();
            tracker.add(new ChunkPos(pos.x , pos.z), this.pos);
            tracker.add(serverPlayerEntity.getGameProfile().getId(), this.pos);
        });

        this.updateBlock();
    }
}
