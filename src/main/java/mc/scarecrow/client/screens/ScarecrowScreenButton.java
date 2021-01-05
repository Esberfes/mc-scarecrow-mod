package mc.scarecrow.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mc.scarecrow.common.block.ScarecrowTile;
import mc.scarecrow.common.network.Networking;
import mc.scarecrow.common.network.packet.ScarecrowTogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.function.Supplier;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

public class ScarecrowScreenButton extends AbstractButton {

    private static final ResourceLocation
            BUTTON_OFF = new ResourceLocation(MOD_IDENTIFIER, "textures/screens/button_off.png"),
            BUTTON_ON = new ResourceLocation(MOD_IDENTIFIER, "textures/screens/button_on.png");

    private final Supplier<ScarecrowTile> tileSupplier;
    private final ScarecrowScreenImage image;

    public ScarecrowScreenButton(int x, int y, Supplier<ScarecrowTile> tileSupplier, World world, ChunkPos chunk) {
        super(x, y, 15, 15, new StringTextComponent(""));
        this.tileSupplier = tileSupplier;
        this.image = new ScarecrowScreenImage(world, chunk);
        this.image.createTexture();
    }

    @Override
    public void onPress() {
        ScarecrowTile tile = this.tileSupplier.get();

        if(tile != null)
            Networking.INSTANCE.sendToServer(new ScarecrowTogglePacket(tile.getPos()));
    }

    @Override
    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(this.isActive() ? BUTTON_ON : BUTTON_OFF);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawTexture(this.x, this.y, 15, 15);

        this.image.updateTexture();
        GlStateManager.enableTexture();
        GlStateManager.bindTexture(this.image.textureId);
        this.drawTexture(this.x + 2, this.y + 2, 11, 11);

        if (!this.isActive())
            fillGradient(matrix, this.x + 2, this.y + 2, this.x + 13, this.y + 13, 0xaa000000, 0xaa000000);

        this.renderBg(matrix, minecraft, mouseX, mouseY);
    }

    public boolean isActive() {
        ScarecrowTile tile = this.tileSupplier.get();

        return tile != null && tile.isForceActive();
    }

    private void drawTexture(int x, int y, int width, int height) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        int z = this.getBlitOffset();
        bufferbuilder.pos(x, y + height, z).tex(1, 0).endVertex();
        bufferbuilder.pos(x + width, y + height, z).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, z).tex(0, 1).endVertex();
        bufferbuilder.pos(x, y, z).tex(0, 0).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }
}
