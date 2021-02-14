package mc.scarecrow.lib.screen;

import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class LibContainerScreenBase extends ContainerScreen<Container> implements IHasContainer<Container> {
    {
        ILibInstanceHandler.fire(this);
    }

    public LibContainerScreenBase(Container screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }
}
