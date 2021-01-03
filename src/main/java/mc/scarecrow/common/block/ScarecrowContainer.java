package mc.scarecrow.common.block;

import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.common.init.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static mc.scarecrow.constant.ScarecrowBlockConstants.*;

public class ScarecrowContainer extends Container {

    private final IInventory chestInventory;
    private final int size = 1;
    private BlockPos blockPos;
    private IItemHandler playerInventory;
    private TileEntity scarecrowTile;
    private PlayerEntity player;

    public ScarecrowContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player) {
        super(ScarecrowMod.TYPE, windowId);
        this.blockPos = pos;
        this.playerInventory = new InvWrapper(playerInventory);
        this.player = player;

        TileEntity tileEntity = world.getTileEntity(pos);
        scarecrowTile = tileEntity;
        assertInventorySize((IInventory) scarecrowTile, INVENTORY_SIZE);
        this.chestInventory = (IInventory) scarecrowTile;
        ((IInventory) scarecrowTile).openInventory(playerInventory.player);

        this.addSlot(new ScarecrowSlot((IInventory) scarecrowTile, 0, 12 + 4 * 18, 8 + 2 * 18));

        int leftCol = (SCREEN_SIZE_X - 162) / 2 + 1;

        for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++) {
            for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++) {
                this.addSlot(new SlotItemHandler(this.playerInventory, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, SCREEN_SIZE_Y - (4 - playerInvRow) * 18 - 10));
            }
        }
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new SlotItemHandler(this.playerInventory, hotbarSlot, leftCol + hotbarSlot * 18, SCREEN_SIZE_Y - 24));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(IWorldPosCallable.of(scarecrowTile.getWorld(), scarecrowTile.getPos()), playerIn, RegistryHandler.scarecrowBlock.get());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < INVENTORY_SIZE) {
                if (!this.mergeItemStack(itemstack1, size, this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!this.mergeItemStack(itemstack1, 0, size, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();

        }

        return itemstack;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
