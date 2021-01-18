package mc.scarecrow.lib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("deprecation")
public class VertexDrawerBuilder {

    private final BufferBuilder bufferBuilder;
    private final Tessellator tessellator;

    private VertexDrawerBuilder() {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        tessellator = Tessellator.getInstance();
        bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    }

    public static VertexDrawerBuilder builder() {
        return new VertexDrawerBuilder();
    }

    @SuppressWarnings("unused")
    public VertexDrawerBuilder vertex(int x, int y, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        return vertex(x, y, red, green, blue, alpha);
    }

    public VertexDrawerBuilder vertex(int x, int y, float red, float green, float blue, float alpha) {
        bufferBuilder.pos(x, y, 0).color(red, green, blue, alpha).endVertex();

        return this;
    }

    public void draw() {
        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }
}
