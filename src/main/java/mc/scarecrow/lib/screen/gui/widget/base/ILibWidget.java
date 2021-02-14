package mc.scarecrow.lib.screen.gui.widget.base;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.event.ILibWidgetEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ILibWidget extends ILibWidgetEventListener {

    void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

    LibVectorBox getDimensionsBox();

    void setDimensionsBox(LibVectorBox vectorBox);

    void init();

    default void update() {
    }

    ;

    default boolean isVisible() {
        return true;
    }

    default void setVisible(boolean visible) {
    }

    void setZ(int z);

    int getZ();
}
