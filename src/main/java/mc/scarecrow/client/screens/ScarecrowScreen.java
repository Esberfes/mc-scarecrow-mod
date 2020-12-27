package mc.scarecrow.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mc.scarecrow.blocks.ScarecrowContainer;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static mc.scarecrow.constant.ScarecrowBlockConstants.*;
import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@OnlyIn(Dist.CLIENT)
public class ScarecrowScreen  extends ContainerScreen<ScarecrowContainer> implements IHasContainer<ScarecrowContainer> {
    private ResourceLocation GUI = new ResourceLocation(MOD_IDENTIFIER, "textures/screens/scarecrow_screen.png");

    private final int textureXSize;

    private final int textureYSize;

    public ScarecrowScreen(ScarecrowContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = SCREEN_SIZE_X;
        this.ySize = SCREEN_SIZE_Y;
        this.textureXSize = SCREEN_TEXTURE_SIZE_X;
        this.textureYSize = SCREEN_TEXTURE_SIZE_Y;

        this.passEvents = false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.func_243248_b(matrixStack, this.title, 8.0F, 6.0F, 4210752);

        this.font.func_243248_b(matrixStack, this.playerInventory.getDisplayName(), 8.0F, (float) (this.ySize - 96 + 2), 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.minecraft.getTextureManager().bindTexture(GUI);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize, this.textureXSize, this.textureYSize);
    }
}