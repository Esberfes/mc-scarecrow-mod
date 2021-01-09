package mc.scarecrow.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mc.scarecrow.common.block.container.ScarecrowContainer;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;
import static mc.scarecrow.constant.ScarecrowScreenConstants.*;

@OnlyIn(Dist.CLIENT)
public class ScarecrowScreen extends ContainerScreen<ScarecrowContainer> implements IHasContainer<ScarecrowContainer> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceLocation GUI = new ResourceLocation(MOD_IDENTIFIER, "textures/screens/scarecrow_screen.png");

    private final int spriteXSize;
    private final int spriteYSize;
    private final ScarecrowTile tile;

    public ScarecrowScreen(ScarecrowContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = INVENTORY_SCREEN_SIZE_X;
        this.ySize = INVENTORY_SCREEN_SIZE_Y;
        this.spriteXSize = SPRITE_TEXTURE_SIZE_X;
        this.spriteYSize = SPRITE_TEXTURE_SIZE_Y;
        this.passEvents = false;

        this.tile = screenContainer.getScarecrowTile();

        setScreenText();
    }

    private void setScreenText() {
        this.titleX = X_OFFSET_GRID;
        this.titleY = 6;

        this.playerInventoryTitleX = X_OFFSET_GRID;
        this.playerInventoryTitleY = this.ySize - 96 + 1;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        try {
            // Main screen print
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int x = (this.width - this.xSize) / 2;
            int y = (this.height - this.ySize) / 2;

            Minecraft.getInstance().getTextureManager().bindTexture(GUI);
            blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize, this.spriteXSize, this.spriteYSize);

            // Flame handle
            if (this.tile.isActive()) {
                int flamePixelsLeft = getBurnLeftScaled();
                int flameOffset = FLAME_HEIGHT - flamePixelsLeft;
                blit(matrixStack,
                        x + 81,
                        y + 47 + flameOffset,
                        this.xSize,
                        flameOffset,
                        14,
                        14 - flameOffset, this.spriteXSize, this.spriteYSize);
            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    private int getBurnLeftScaled() {
        int currentBurnTime = this.tile.getCurrentBurnTime();
        int totalItemBurnTime = this.tile.getTotalItemBurnTime();
        float percentage = currentBurnTime == 0 || totalItemBurnTime == 0 ? 0 : (float) ((currentBurnTime * 100) / totalItemBurnTime);
        return (int) Math.ceil(FLAME_HEIGHT * percentage / 100);
    }
}
