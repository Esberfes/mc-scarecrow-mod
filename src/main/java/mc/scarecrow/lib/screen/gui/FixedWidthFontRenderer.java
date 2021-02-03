package mc.scarecrow.lib.screen.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mc.scarecrow.lib.builder.screen.TextBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@OnlyIn(Dist.CLIENT)
public class FixedWidthFontRenderer {
    private static final Matrix4f IDENTITY = TransformationMatrix.identity().getMatrix();
    public static final ResourceLocation FONT = new ResourceLocation(MOD_IDENTIFIER, "textures/fonts/term_font.png");


    public static final float WIDTH = 256.0f;
    private static float scale = 0.7F;
    private static float rescale = 1F / scale;
    public static RenderType TYPE = Type.MAIN;
    public static float FONT_HEIGHT = (9 * scale);
    public static float IN_FONT_HEIGHT = 9;
    public static float FONT_WIDTH = (6 * scale);
    public static float IN_FONT_WIDTH = 6;
    public static float OUT_FONT_WIDTH = FONT_WIDTH * scale;
    public static float OUT_FONT_HEIGHT = FONT_HEIGHT * scale;

    private FixedWidthFontRenderer() {
    }


    public static void drawString(Matrix4f transform, IVertexBuilder renderer, float x, float y, int z, TextBuffer text) {
        for (int i = 0; i < text.length(); i++) {
            // Draw char
            int index = text.charAt(i);
            if (index > 255) index = '?';
            drawChar(transform, renderer, x + i * (FONT_WIDTH * scale), y, z, index, 0, 0, 0);
        }

    }

    public static void drawString(float x, float y, int z, TextBuffer text) {
        bindFont();
        Matrix4f transform = TransformationMatrix.identity().getMatrix();
        IRenderTypeBuffer.Impl renderer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        transform.mul(scale);
        drawString(transform, ((IRenderTypeBuffer) renderer).getBuffer(Type.MAIN), x, y, z, text);
        IDENTITY.mul(rescale);
        renderer.finish();
    }

    private static void drawChar(Matrix4f transform, IVertexBuilder buffer, float x, float y, int z, int index, float r, float g, float b) {
        // Short circuit to avoid the common case - the texture should be blank here after all.
        if (index == '\0' || index == ' ') return;

        int column = index % 16;
        int row = index / 16;

        float xStart = 1 + column * (IN_FONT_WIDTH + 2);
        float yStart = 1 + row * (IN_FONT_HEIGHT + 2);

        x = ((x) * rescale);
        y = ((y) * rescale);
        z = (int) ((z) * rescale);

        buffer.pos(transform, x, y, z).color(r, g, b, 1.0f).tex(xStart / WIDTH, yStart / WIDTH).endVertex();
        buffer.pos(transform, x, y + FONT_HEIGHT, z).color(r, g, b, 1.0f).tex(xStart / WIDTH, (yStart + IN_FONT_HEIGHT) / WIDTH).endVertex();
        buffer.pos(transform, x + FONT_WIDTH, y, z).color(r, g, b, 1.0f).tex((xStart + IN_FONT_WIDTH) / WIDTH, yStart / WIDTH).endVertex();
        buffer.pos(transform, x + FONT_WIDTH, y, z).color(r, g, b, 1.0f).tex((xStart + IN_FONT_WIDTH) / WIDTH, yStart / WIDTH).endVertex();
        buffer.pos(transform, x, y + FONT_HEIGHT, z).color(r, g, b, 1.0f).tex(xStart / WIDTH, (yStart + IN_FONT_HEIGHT) / WIDTH).endVertex();
        buffer.pos(transform, x + FONT_WIDTH, y + FONT_HEIGHT, z).color(r, g, b, 1.0f).tex((xStart + IN_FONT_WIDTH) / WIDTH, (yStart + IN_FONT_HEIGHT) / WIDTH).endVertex();
    }

    private static void bindFont() {
        Minecraft.getInstance().getTextureManager().bindTexture(FONT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
    }

    private static final class Type extends RenderState {
        private static final int GL_MODE = GL11.GL_TRIANGLES;

        private static final VertexFormat FORMAT = DefaultVertexFormats.POSITION_COLOR_TEX;

        static final RenderType MAIN = RenderType.makeType(
                "terminal_font", FORMAT, GL_MODE, 1024,
                false, false, // useDelegate, needsSorting
                RenderType.State.getBuilder()
                        .texture(new RenderState.TextureState(FONT, false, false)) // blur, minimap
                        .alpha(DEFAULT_ALPHA)
                        .lightmap(LIGHTMAP_DISABLED)
                        .writeMask(COLOR_WRITE)
                        .build(false)
        );

        static final RenderType BLOCKER = RenderType.makeType(
                "terminal_blocker", FORMAT, GL_MODE, 256,
                false, false, // useDelegate, needsSorting
                RenderType.State.getBuilder()
                        .texture(new RenderState.TextureState(FONT, false, false)) // blur, minimap
                        .alpha(DEFAULT_ALPHA)
                        .writeMask(DEPTH_WRITE)
                        .lightmap(LIGHTMAP_DISABLED)
                        .build(false)
        );

        private Type(String name, Runnable setup, Runnable destroy) {
            super(name, setup, destroy);
        }
    }
}
