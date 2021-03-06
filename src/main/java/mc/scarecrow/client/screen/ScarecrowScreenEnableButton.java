package mc.scarecrow.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mc.scarecrow.lib.utils.MapBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static mc.scarecrow.client.screen.OnButtonPressedCommandConsumer.networkCommandSubscription;
import static mc.scarecrow.client.screen.ScarecrowScreen.GUI;
import static mc.scarecrow.constant.ScarecrowScreenConstants.*;
import static mc.scarecrow.lib.utils.GuiUtils.drawGradientRectIf;

@OnlyIn(Dist.CLIENT)
public class ScarecrowScreenEnableButton extends AbstractButton {

    private final BlockPos tilePos;
    private boolean isActive;

    public ScarecrowScreenEnableButton(int x, int y, BlockPos tilePos) {
        super(x, y, BUTTON_SIZE_X, BUTTON_SIZE_Y, new StringTextComponent(""));
        this.tilePos = tilePos;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().getTextureManager().bindTexture(GUI);

        matrixStack.push();
        RenderSystem.enableBlend();

        float scale = isHovered() ? 1.1F : 1.0f;
        matrixStack.scale(scale, scale, 1F);

        blit(matrixStack,
                (int) (x / scale),
                (int) (y / scale),
                INVENTORY_SCREEN_SIZE_X,
                BUTTON_ON_BOX_Y_OFFSET + (isActive ? 0 : BUTTON_SIZE_Y),
                width,
                height,
                SPRITE_TEXTURE_SIZE_X,
                SPRITE_TEXTURE_SIZE_Y
        );
        RenderSystem.disableBlend();
        matrixStack.pop();

        drawGradientRectIf(x, y, x + BUTTON_SIZE_X, y + BUTTON_SIZE_Y, 847940234, 1681313342, this::isHovered);
    }

    @Override
    public void onPress() {
        networkCommandSubscription.execute(networkCommandSubscription.commandBuilder()
                .payload(new MapBuilder<String, Object>()
                        .entry("pos", tilePos.toLong()))
                .build()
        );
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
