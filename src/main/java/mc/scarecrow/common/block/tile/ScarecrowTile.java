package mc.scarecrow.common.block.tile;

import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.container.ScarecrowContainer;
import mc.scarecrow.common.init.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.INVENTORY_SIZE;

public class ScarecrowTile extends LockableLootTileEntity implements ITickable {

    private static final Logger LOGGER = LogManager.getLogger();

    private NonNullList<ItemStack> chestContents;
    private int numPlayersUsing;

    public ScarecrowTile() {
        super(RegistryHandler.scarecrowTileBlock.get());
        numPlayersUsing = 0;
        chestContents = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return chestContents;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        chestContents = itemsIn;
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new StringTextComponent("Scarecrow");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return null;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player, PlayerEntity entity) {
        try {
            if (world == null)
                throw new Exception("World is null on create menu");

            return new ScarecrowContainer(id, world, getPos(), player, entity);

        } catch (Throwable e) {
            LOGGER.error(e);
            return null;
        }
    }

    @Override
    public int getSizeInventory() {
        return chestContents.size();
    }

    @Override
    public void tick() {

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

    private void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        if (this.world != null && block instanceof ScarecrowBlock) {
            this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, block);
        }
    }

    // TODO real implementation by fuel status condition

    /**
     * Called from WorldTick event when checked to keep chunk enabled
     *
     * @return this tile is active
     */
    public boolean isActive() {
        return true;
    }
}