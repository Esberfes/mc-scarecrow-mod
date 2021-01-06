package mc.scarecrow.common.block.container;

import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.init.RegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static mc.scarecrow.common.init.events.ContainersRegisterEventHandler.CONTAINER_TYPE;
import static mc.scarecrow.constant.ScarecrowBlockConstants.*;

public class ScarecrowContainer extends Container {
    private final World world;
    private final ScarecrowTile scarecrowTile;
    private final IItemHandler playerInventory;

    public ScarecrowContainer(int id, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity entity) {
        super(CONTAINER_TYPE, id);
        this.world = world;
        scarecrowTile = (ScarecrowTile) world.getTileEntity(pos);
        this.playerInventory = new InvWrapper(playerInventory);

        assert scarecrowTile != null;
        ((IInventory) scarecrowTile).openInventory(playerInventory.player);

        InitInventory();
    }

    private void InitInventory() {
        this.addSlot(new ScarecrowContainerItemSlot(scarecrowTile, 0, 12 + 4 * 18, 8 + 2 * 18));

        int leftCol = (SCREEN_SIZE_X - 162) / 2 + 1;

        for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++) {
            for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++) {
                this.addSlot(new SlotItemHandler(
                        this.playerInventory,
                        playerInvCol + playerInvRow * 9 + 9,
                        leftCol + playerInvCol * 18,
                        SCREEN_SIZE_Y - (4 - playerInvRow) * 18 - 10)
                );
            }
        }
        for (int hotBarSlot = 0; hotBarSlot < 9; hotBarSlot++) {
            this.addSlot(new SlotItemHandler(this.playerInventory,
                    hotBarSlot,
                    leftCol + hotBarSlot * 18, SCREEN_SIZE_Y - 24)
            );
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStackAux = slot.getStack();
            itemStack = itemStackAux.copy();

            if (index < INVENTORY_SIZE) {
                if (!this.mergeItemStack(itemStackAux, INVENTORY_SIZE, this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!this.mergeItemStack(itemStackAux, 0, INVENTORY_SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStackAux.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();

        }

        return itemStack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(IWorldPosCallable.of(world, scarecrowTile.getPos()), playerIn, RegistryHandler.scarecrowBlock.get());
    }
}
