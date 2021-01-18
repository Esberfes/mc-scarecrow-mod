package mc.scarecrow.lib.utils;

import mc.scarecrow.lib.gui.VertexDrawerBuilder;

import java.util.function.Supplier;

public abstract class GuiUtils {

    private GuiUtils() {
    }

    public static void drawGradientRectIf(int x0, int y0, int x1, int y1, int colour0, int colour1, Supplier<Boolean> supplier) {
        if (supplier.get())
            drawGradientRect(x0, y0, x1, y1, colour0, colour1);
    }

    public static void drawGradientRect(int x0, int y0, int x1, int y1, int colour0, int colour1) {
        float alpha0 = (colour0 >> 24 & 255) / 255.0F;
        float blue0 = (colour0 >> 16 & 255) / 255.0F;
        float green0 = (colour0 >> 8 & 255) / 255.0F;
        float red0 = (colour0 & 255) / 255.0F;
        float alpha1 = (colour1 >> 24 & 255) / 255.0F;
        float blue1 = (colour1 >> 16 & 255) / 255.0F;
        float green1 = (colour1 >> 8 & 255) / 255.0F;
        float red1 = (colour1 & 255) / 255.0F;

        VertexDrawerBuilder.builder()
                .vertex(x1, y0, blue0, green0, red0, alpha0)
                .vertex(x0, y0, blue0, green0, red0, alpha0)
                .vertex(x0, y1, blue1, green1, red1, alpha1)
                .vertex(x1, y1, blue1, green1, red1, alpha1)
                .draw();
    }
}
