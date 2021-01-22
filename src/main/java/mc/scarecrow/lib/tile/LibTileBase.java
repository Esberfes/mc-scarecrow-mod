package mc.scarecrow.lib.tile;

import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class LibTileBase extends LockableLootTileEntity {
    {
        ILibInstanceHandler.fire(this);
    }

    protected LibTileBase(TileEntityType<?> typeIn) {
        super(typeIn);
    }
}
