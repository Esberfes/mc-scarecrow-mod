package mc.scarecrow.lib.tile;

import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

public abstract class LbTileEntityRenderBase extends TileEntityRenderer<TileEntity> {
    {
        ILibInstanceHandler.fire(this);
    }

    public LbTileEntityRenderBase(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }
}
