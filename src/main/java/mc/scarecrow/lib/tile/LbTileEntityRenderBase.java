package mc.scarecrow.lib.tile;

import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class LbTileEntityRenderBase extends TileEntityRenderer<TileEntity> {
    {
        ILibInstanceHandler.fire(this);
    }

    public LbTileEntityRenderBase(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }
}
