package mc.scarecrow.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.common.block.ScarecrowContainer;
import mc.scarecrow.common.block.ScarecrowTile;
import mc.scarecrow.common.network.Networking;
import mc.scarecrow.common.network.packet.ScarecrowTogglePacket;
import mc.scarecrow.exception.NotEntityFoundException;
import mc.scarecrow.exception.NotEntityOfTypeException;
import mc.scarecrow.exception.ScarecrowException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mc.scarecrow.constant.ScarecrowBlockConstants.*;
import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@OnlyIn(Dist.CLIENT)
public class ScarecrowScreen extends ContainerScreen<ScarecrowContainer> implements IHasContainer<ScarecrowContainer> {
    private ResourceLocation GUI = new ResourceLocation(MOD_IDENTIFIER, "textures/screens/scarecrow_screen.png");
    private ResourceLocation LEFT_GUI = new ResourceLocation(MOD_IDENTIFIER, "textures/screens/left.png");
    private static final Logger LOGGER = LogManager.getLogger();
    private final int textureXSize;
    private final int textureYSize;

    private int asideH = 184;
    private int asideW = 92;

    private int cw = 150;
    private int ch = 20;

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
        try {
            ScarecrowTile scarecrowTile = getTileOrClose();
            this.renderBackground(matrixStack);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            this.renderHoveredTooltip(matrixStack, mouseX, mouseY);

            int x = (this.width - this.xSize) / 2;
            int y = (this.height - this.ySize) / 2;
            getMinecraft().fontRenderer.drawString(matrixStack, "Owner: " + scarecrowTile.getOwnerName(), this.guiLeft + -95, y + 5, 0x404040);
            getMinecraft().fontRenderer.drawString(matrixStack, "T.Fuel: " + toTime(scarecrowTile.getTotalBurnTime()), this.guiLeft + -95, y + 40, 0x404040);
            getMinecraft().fontRenderer.drawString(matrixStack, "Fuel: " + toTime(scarecrowTile.getCurrentBurningTime()), this.guiLeft + -95, y + 50, 0x404040);
            getMinecraft().fontRenderer.drawString(matrixStack, "Active: " + (scarecrowTile.isActive() ? "yes" : "no"), this.guiLeft + -95, y + 60, 0x404040);

            CheckboxButton checkboxButton = new CheckboxButton(this.guiLeft + -95, y + 80, cw, ch,
                    new StringTextComponent("Enabled"), scarecrowTile.isForceActive()) {

                @Override
                public void onClick(double mouseX, double mouseY) {
                    Networking.INSTANCE.sendToServer(new ScarecrowTogglePacket(scarecrowTile.getPos()));
                }

                @Override
                public void onPress() {

                }

                @Override
                public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
                    this.setMessage(this.active ? new StringTextComponent("Enabled") : new StringTextComponent("Paused"));
                    super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
                }

                @Override
                public boolean isChecked() {
                    return scarecrowTile.isForceActive();
                }
            };

            this.addButton(checkboxButton);

        } catch (ScarecrowException e) {
            ScarecrowMod.PROXY.getPlayerEntity().closeScreen();
        } catch (Throwable e) {
            LOGGER.error(e);
            ScarecrowMod.PROXY.getPlayerEntity().closeScreen();
        }
    }

    public String toTime(int ticks) {
        int seconds = ticks / 20;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.func_243248_b(matrixStack, this.title, 8.0F, 6.0F, 4210752);
        this.font.func_243248_b(matrixStack, this.playerInventory.getDisplayName(), 8.0F, (float) (this.ySize - 96 + 2), 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        Minecraft.getInstance().getTextureManager().bindTexture(GUI);
        blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize, this.textureXSize, this.textureYSize);
        Minecraft.getInstance().getTextureManager().bindTexture(LEFT_GUI);
        blit(matrixStack, this.guiLeft + -101, this.guiTop, 0, 0, 92, 184, 92, 184);
    }

    public ScarecrowTile getTileOrClose() throws NotEntityOfTypeException, NotEntityFoundException {
        TileEntity tileEntity = ScarecrowMod.PROXY.getPlayerWorld().getTileEntity(container.getBlockPos());

        if (tileEntity == null)
            throw new NotEntityFoundException(ScarecrowTile.class, container.getBlockPos());

        if (!(tileEntity instanceof ScarecrowTile))
            throw new NotEntityOfTypeException(ScarecrowTile.class);

        return (ScarecrowTile) tileEntity;
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}