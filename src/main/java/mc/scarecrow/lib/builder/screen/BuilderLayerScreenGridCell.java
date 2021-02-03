package mc.scarecrow.lib.builder.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.LibWidgetEventPropagationCanceler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static mc.scarecrow.lib.utils.UIUtils.drawBox;


@OnlyIn(Dist.CLIENT)
public class BuilderLayerScreenGridCell implements ILibWidget {
    {
        ILibInstanceHandler.fire(this);
    }

    private LibVectorBox dimensions;
    private boolean isHovering;
    private boolean clicked;
    private long lastUpdate;
    private int z;

    public BuilderLayerScreenGridCell(LibVectorBox dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        long now = System.currentTimeMillis();
        long timeElapsed = now - lastUpdate;

        float clickInc = clicked ? 30 : 0;

        if (this.isHovering)
            drawBox(dimensions, 2, 191, 212 + clickInc, 223, 1);
        else
            drawBox(dimensions, 2, 144, 164 + clickInc, 174, 1);

        if (timeElapsed >= 200) {
            clicked = false;
            lastUpdate = now;
        }
    }

    @Override
    public LibVectorBox getDimensionsBox() {
        return dimensions;
    }

    @Override
    public void setDimensionsBox(LibVectorBox vectorBox) {
        this.dimensions = vectorBox;
    }

    @Override
    public void init() {
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public void onClick(LibVector2D vector2D, int button) {
        this.clicked = true;
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        this.isHovering = true;
        LibWidgetEventPropagationCanceler.cancelPropagation();
    }

    @Override
    public void onHoverOut() {
        if (this.isHovering) {
            this.isHovering = false;
            LibWidgetEventPropagationCanceler.cancelPropagation();
        }
    }
}