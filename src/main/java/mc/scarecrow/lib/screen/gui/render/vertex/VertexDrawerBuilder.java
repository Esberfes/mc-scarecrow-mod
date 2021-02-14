package mc.scarecrow.lib.screen.gui.render.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public class VertexDrawerBuilder {

    private final BufferBuilder bufferBuilder;
    private final Tessellator tessellator;
    private final int z;
    private final List<LibVertex> vertices;

    private VertexDrawerBuilder(int z) {
        this.z = z;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        tessellator = Tessellator.getInstance();
        bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertices = new LinkedList<>();
    }

    public static VertexDrawerBuilder builder() {
        return builder(0);
    }

    public static VertexDrawerBuilder builder(int z) {
        return new VertexDrawerBuilder(z);
    }

    @SuppressWarnings("unused")
    public VertexDrawerBuilder vertex(int x, int y, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        this.vertices.add(new LibVertex(x, y, z, red, green, blue, alpha));

        return this;
    }

    public VertexDrawerBuilder vertex(int x, int y, float red, float green, float blue, float alpha) {
        bufferBuilder.pos(x, y, z).color(red / 255.0F, green / 255.0F, blue / 255.0F, alpha).endVertex();

        return this;
    }

    public VertexDrawerBuilder vertex(float x, float y, float red, float green, float blue, float alpha) {
        this.vertices.add(new LibVertex(x, y, z, red, green, blue, alpha));

        return this;
    }

    public VertexDrawerBuilder vertex(LibVertex vertex) {
        this.vertices.add(vertex);

        return this;
    }

    public void draw() {
        this.vertices.forEach(v -> bufferBuilder.pos(
                v.getVector3D().getX(),
                v.getVector3D().getY(),
                v.getVector3D().getZ())
                .color(
                        v.getColor().getRed(),
                        v.getColor().getGreen(),
                        v.getColor().getBlue(),
                        v.getColor().getAlpha()
                ).endVertex());

        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }
}
