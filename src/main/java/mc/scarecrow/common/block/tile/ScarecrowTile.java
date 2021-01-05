package mc.scarecrow.common.block.tile;

import mc.scarecrow.common.init.RegistryHandler;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class ScarecrowTile extends LockableLootTileEntity implements ITickable {
    public ScarecrowTile() {
        super(RegistryHandler.scarecrowTileBlock.get());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return null;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {

    }

    @Override
    protected ITextComponent getDefaultName() {
        return null;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return null;
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public void tick() {

    }
}
