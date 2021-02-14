package mc.scarecrow.lib.screen.gui.widget.event;

import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.screen.gui.widget.base.ILibWidget;

import java.util.Comparator;

public interface ILibWidgetEventListener extends ILibWidgetFocusable {

    void onClick(LibVector2D vector2D, int button);

    default void onClickRelease(LibVector2D vector2D, int button) {
    }

    void onHover(LibVector2D vector2D);

    void onHoverOut();

    default void onMouseScrolled(LibVector2D vector2D, double delta) {
    }

    default void charTyped(char codePoint, int modifiers) {
    }

    default void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    static Comparator<ILibWidget> getPriorityComparator() {
        return Comparator.comparingInt(ILibWidget::getZ);
    }
}
