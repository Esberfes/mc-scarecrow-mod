package mc.scarecrow.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mc.scarecrow.common.block.container.ScarecrowContainer;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.screen.LibContainerScreenBase;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;
import static mc.scarecrow.constant.ScarecrowScreenConstants.*;

@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public class ScarecrowScreen extends LibContainerScreenBase {

    public static final ResourceLocation GUI = new ResourceLocation(MOD_IDENTIFIER, "textures/screens/scarecrow_screen.png");

    @LibInject
    private Logger logger;

    private final int spriteXSize;
    private final int spriteYSize;
    private final ScarecrowTile tile;

    private int centeredGuiX;
    private int centeredGuiY;

    private ScarecrowScreenEnableButton scarecrowScreenEnableButton;

    public ScarecrowScreen(Container screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = INVENTORY_SCREEN_SIZE_X;
        this.ySize = INVENTORY_SCREEN_SIZE_Y;
        this.spriteXSize = SPRITE_TEXTURE_SIZE_X;
        this.spriteYSize = SPRITE_TEXTURE_SIZE_Y;
        this.passEvents = false;

        if (!(screenContainer instanceof ScarecrowContainer))
            throw new IllegalArgumentException("Invalid type, should be instance of ScarecrowContainer");

        this.tile = ((ScarecrowContainer) screenContainer).getScarecrowTile();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        this.centeredGuiX = (width - this.xSize) / 2;
        this.centeredGuiY = (height - this.ySize) / 2;

        this.titleX = X_OFFSET_GRID;
        this.titleY = 6;

        this.playerInventoryTitleX = X_OFFSET_GRID;
        this.playerInventoryTitleY = this.ySize - 96 + 1;

        this.scarecrowScreenEnableButton
                = new ScarecrowScreenEnableButton(this.centeredGuiX + 160, this.centeredGuiY + 70, tile.getPos());
        this.addButton(scarecrowScreenEnableButton);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        try {
            // Main screen print
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(GUI);
            blit(matrixStack, this.centeredGuiX, this.centeredGuiY, 0, 0, this.xSize, this.ySize, this.spriteXSize, this.spriteYSize);

            this.scarecrowScreenEnableButton.setActive(this.tile.isActive());

            // Flame handle
            if (this.tile.isActive()) {
                int flamePixelsLeft = getBurnLeftScaled();
                int flameOffset = FLAME_HEIGHT - flamePixelsLeft;
                blit(matrixStack,
                        this.centeredGuiX + 81,
                        this.centeredGuiY + 47 + flameOffset,
                        this.xSize,
                        flameOffset,
                        14,
                        14 - flameOffset, this.spriteXSize, this.spriteYSize);
            }
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    private int getBurnLeftScaled() {
        int currentBurnTime = this.tile.getCurrentBurnTime();
        int totalItemBurnTime = this.tile.getTotalItemBurnTime();
        float percentage = currentBurnTime == 0 || totalItemBurnTime == 0 ? 0 : (float) ((currentBurnTime * 100) / totalItemBurnTime);
        return (int) Math.ceil(FLAME_HEIGHT * percentage / 100);
    }
}
