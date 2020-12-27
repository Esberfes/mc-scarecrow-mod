package mc.scarecrow.blocks;

import mc.scarecrow.init.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import static mc.scarecrow.constant.ScarecrowBlockConstants.*;

public class ScarecrowContainer extends Container {

    private final IInventory chestInventory;
    private final int size = 1;

    public ScarecrowContainer(ContainerType<?> type, int windowId, PlayerInventory playerInventory, IInventory inventory) {
        super(type, windowId);
        assertInventorySize(inventory, INVENTORY_SIZE);
        this.chestInventory = inventory;
        inventory.openInventory(playerInventory.player);

        this.addSlot(new ScarecrowSlot(inventory, 0, 12 + 4 * 18, 8 + 2 * 18));

        int leftCol = (SCREEN_SIZE_X - 162) / 2 + 1;

        for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++) {
            for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++) {
                this.addSlot(new Slot(playerInventory, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, SCREEN_SIZE_Y - (4 - playerInvRow) * 18 - 10));
            }
        }
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(playerInventory, hotbarSlot, leftCol + hotbarSlot * 18, SCREEN_SIZE_Y - 24));
        }
    }

    public ScarecrowContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInventory) {
        this(containerType, windowId, playerInventory, new Inventory(1));
    }

    public ScarecrowContainer(int windowId, PlayerInventory playerInventory) {
        this(RegistryHandler.scarecrowBlockContainer.get(), windowId, playerInventory);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.chestInventory.isUsableByPlayer(playerIn);
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
}
