package mc.scarecrow.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import javafx.stage.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TextComponent;

public class ScarecrowWidget extends Widget {

    public ScarecrowWidget(int x, int y, int width, int height, Screen parent, TextComponent title, TextComponent... components) {
        super(x, y, width, height, title);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible)
            return;


    }

}
