package mc.scarecrow.lib.builder.screen;

import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.implementation.LibWidgetButton;
import mc.scarecrow.lib.screen.gui.widget.implementation.LibWidgetPanel;

public class BuilderLayerScreenMenu extends LibWidgetPanel {

    private LibWidgetButton previousLayer;
    private LibWidgetButton nextLayer;

    public BuilderLayerScreenMenu(LibVectorBox dimensions, int z, float red, float green, float blue, float alpha) {
        super(dimensions, z, red, green, blue, alpha);
    }

    @Override
    protected void onInitStart() {

    }
}
