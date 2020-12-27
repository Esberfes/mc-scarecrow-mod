package mc.scarecrow.blocks;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;

public class ScarecrowSlot extends Slot {

    public ScarecrowSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        //return net.minecraftforge.common.ForgeHooks.getBurnTime(stack) > 0;
        return stack.getItem() instanceof EnderPearlItem;
    }
}
