package mc.scarecrow.lib.math;

public class LibRGBA {

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    public LibRGBA(float red, float green, float blue) {
        this(red, green, blue, 1F);
    }

    public LibRGBA(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public static LibRGBA fromIntColor(int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        return new LibRGBA(red, green, blue, alpha);
    }
}
