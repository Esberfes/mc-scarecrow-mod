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
import static mc.scarecrow.constant.ScarecrowBlockConstants.INVENTORY_SIZE;
import static mc.scarecrow.constant.ScarecrowScreenConstants.*;

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

        assignSlotsToInventoryWindow();
    }

    private void assignSlotsToInventoryWindow() {
        // Input slot
        this.addSlot(new ScarecrowContainerItemSlot(scarecrowTile,
                0,
                X_OFFSET_GRID + SLOT_BORDER_SIZE + (4 * SLOT_SIZE),
                Y_OFFSET_INPUT_SLOT + SLOT_BORDER_SIZE));

        // Inventory slots
        int leftCol = X_OFFSET_GRID;
        for (int playerInvRow = 0; playerInvRow < NUMBER_ROWS; playerInvRow++) {
            for (int playerInvCol = 0; playerInvCol < NUMBER_COLUMNS; playerInvCol++) {
                this.addSlot(new SlotItemHandler(
                        this.playerInventory,
                        playerInvCol + NUMBER_COLUMNS + (playerInvRow * 9),
                        leftCol + SLOT_BORDER_SIZE + (playerInvCol * SLOT_SIZE),
                        Y_OFFSET_GRID + SLOT_BORDER_SIZE + (playerInvRow * SLOT_SIZE))
                );
            }
        }

        // Hotbar slots
        for (int hotBarSlot = 0; hotBarSlot < NUMBER_COLUMNS; hotBarSlot++) {
            this.addSlot(new SlotItemHandler(this.playerInventory,
                    hotBarSlot,
                    leftCol + SLOT_BORDER_SIZE + (hotBarSlot * SLOT_SIZE),
                    INVENTORY_SCREEN_SIZE_Y - 24)
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

    public ScarecrowTile getScarecrowTile() {
        return scarecrowTile;
    }
}
