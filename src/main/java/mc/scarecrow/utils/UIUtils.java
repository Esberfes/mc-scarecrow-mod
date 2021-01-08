package mc.scarecrow.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;

public abstract class UIUtils {

    private UIUtils() {
    }

    /**
     * Get formatted time in hh:mm:ss from ticks asumming that 1 = 0.05 seconds so 1 second = 20 ticks
     *
     * @param ticks to parse in time
     * @return formatted string
     */
    public static String ticksToTime(int ticks) {
        int seconds = ticks / 20;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Draw text in the current screen, this should be use after everything is draw so evaluate the event type like this:
     * RenderGameOverlayEvent.Post event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE then can draw
     *
     * @param font        FontRenderer, Minecraft.getInstance().fontRenderer
     * @param matrixStack MatrixStack that comes from event
     * @param text        Text to draw
     * @param xPosition   Offset left in pixels
     * @param yPosition   Offset top in pixels
     * @param color       Integer representation of a color, use static Color.fromInt() or from instance color.getColor(), Color.fromHex("#FFFFFF").getColor()
     */
    private static void drawText(FontRenderer font, MatrixStack matrixStack, String text, Float xPosition, Float yPosition, Integer color) {
        font.drawStringWithShadow(matrixStack, text, xPosition, yPosition, color);
    }

}
