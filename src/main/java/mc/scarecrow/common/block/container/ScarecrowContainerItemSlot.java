package mc.scarecrow.common.block.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ScarecrowContainerItemSlot extends Slot {

    public ScarecrowContainerItemSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        // TODO implement restrictions
        return false;
    }
}
