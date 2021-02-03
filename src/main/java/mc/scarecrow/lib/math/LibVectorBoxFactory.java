package mc.scarecrow.lib.math;

import java.util.function.BiFunction;

public enum LibVectorBoxFactory {
    TOP_LEFT((bounds, padding) -> {
        return new LibVector2D(bounds.getLeftTop().getX() - padding, bounds.getLeftTop().getY() + padding);
    }),
    TOP_RIGHT((bounds, padding) -> {
        return new LibVector2D(bounds.getRightTop().getX() - padding, bounds.getRightTop().getY() + padding);
    }),
    BOTTOM_LEFT((bounds, padding) -> {
        return new LibVector2D(bounds.getLeftBottom().getX() + padding, bounds.getLeftBottom().getY() - padding);
    }),
    BOTTOM_RIGHT((bounds, padding) -> {
        return new LibVector2D(bounds.getRightBottom().getX() - padding, bounds.getRightBottom().getY() - padding);
    });

    private final BiFunction<LibVectorBox, Integer, LibVector2D> factory;

    LibVectorBoxFactory(BiFunction<LibVectorBox, Integer, LibVector2D> factory) {
        this.factory = factory;
    }

    public LibVector2D build(LibVectorBox bounds, int padding) {
        return factory.apply(bounds, padding);
    }
}