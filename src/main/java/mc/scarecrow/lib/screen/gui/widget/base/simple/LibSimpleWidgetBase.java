package mc.scarecrow.lib.screen.gui.widget.base.simple;

import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.base.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.base.advance.LibAdvancedWidgetBase;
import mc.scarecrow.lib.screen.gui.widget.focus.FocusToken;
import org.apache.logging.log4j.Logger;

/**
 * This widget will not handle events and even not propagate, use only to complement another widgets or something simple.
 * For more complex implementations {@link LibAdvancedWidgetBase}
 */
public abstract class LibSimpleWidgetBase implements ILibWidget {
    {
        ILibInstanceHandler.fire(this);
    }

    @LibInject
    protected Logger logger;
    protected LibVectorBox dimensions;
    protected int z;
    protected float red;
    protected float green;
    protected float blue;
    protected float alpha;
    protected boolean visible;

    public LibSimpleWidgetBase(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        this.dimensions = dimensions;
        this.z = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.visible = true;
    }

    @Override
    public void init() {

    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void onMouseScrolled(LibVector2D vector2D, double delta) {
    }

    @Override
    public void onClick(LibVector2D vector2D, int button) {
    }

    @Override
    public void onClickRelease(LibVector2D vector2D, int button) {
    }

    @Override
    public void onHover(LibVector2D vector2D) {
    }

    @Override
    public void onHoverOut() {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return dimensions;
    }

    @Override
    public void onTokenReceived(FocusToken token) {
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = vectorBox;
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

    public void setDimensions(LibVectorBox dimensions) {
        this.dimensions = dimensions;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
