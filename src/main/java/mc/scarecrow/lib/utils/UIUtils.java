package mc.scarecrow.lib.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

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
    public static void drawText(FontRenderer font, MatrixStack matrixStack, String text, Float xPosition, Float yPosition, Integer color) {
        font.drawStringWithShadow(matrixStack, text, xPosition, yPosition, color);
    }

    public static double calcAngle(Direction direction, double target, double lastYaw, int delta) {
        double dirAngle = directionToAngle(direction);

        double goalTarget = ((target + dirAngle) * -1);

        while (lastYaw - goalTarget < -180.0F)
            goalTarget -= 360.0F;

        while (lastYaw - goalTarget >= 180.0F)
            goalTarget += 360.0F;

        double targetAngle = goalTarget < 0D ? Math.max(goalTarget, -delta) : Math.min(goalTarget, delta);

        return targetAngle < 0D ? Math.max(targetAngle + lastYaw, goalTarget) : Math.min(targetAngle + lastYaw, goalTarget);
    }

    public static double lookAt(Vector3d origin, Vector3i targetFacingDirection) {
        return lookAt(origin, origin.add(new Vector3d(targetFacingDirection.getX(), targetFacingDirection.getY(), targetFacingDirection.getZ())));
    }

    public static double lookAt(Vector3d origin, Entity target) {
        return lookAt(origin, target.getPositionVec());
    }

    public static double lookAt(Vector3d origin, Vector3d targetVector) {
        double d0 = targetVector.x - origin.x;
        double d2 = targetVector.z - origin.z;

        return (MathHelper.wrapDegrees((MathHelper.atan2(d2, d0) * (180F / Math.PI)) - 90.0F));
    }

    public static double directionToAngle(Direction direction) {
        double dirAngle;
        switch (direction) {
            case NORTH:
            case EAST:
                dirAngle = 180D;
                break;
            case SOUTH:
            case WEST:
                dirAngle = -180D;
                break;
            default:
                dirAngle = 0D;
        }

        return dirAngle;
    }
}
