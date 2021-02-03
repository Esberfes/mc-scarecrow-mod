package mc.scarecrow.lib.builder.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.math.LibVector2D;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BuilderLayerScreenCloseButton extends AbstractButton {
    {
        ILibInstanceHandler.fire(this);
    }

    private final Runnable action;

    public BuilderLayerScreenCloseButton(LibVector2D position, Runnable action) {
        super(position.getX() - 12, position.getY(), 12, 12, new StringTextComponent("x"));
        this.action = action;
    }

    public void onPress() {
        this.action.run();
    }

    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);

        if (this.isHovered())
            this.renderToolTip(matrixStack, mouseX, mouseY);
    }

}
