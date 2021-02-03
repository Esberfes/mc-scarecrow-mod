package mc.scarecrow.lib.screen.gui.widget;

import mc.scarecrow.lib.math.LibVector2D;

import java.util.Comparator;

public interface ILibWidgetEventListener extends ILibWidgetFocusable {

    void onClick(LibVector2D vector2D, int button);

    default void onClickRelease(LibVector2D vector2D, int button) {
    }

    ;

    void onHover(LibVector2D vector2D);

    void onHoverOut();

    default void onMouseScrolled(LibVector2D vector2D, double delta) {
    }

    default Priority getPriority() {
        return Priority.low;
    }

    default void charTyped(char codePoint, int modifiers) {
    }

    default void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    public static enum Priority {
        max, high, medium, low;
    }

    static Comparator<ILibWidget> getPriorityComparator() {
        return new Comparator<ILibWidget>() {
            @Override
            public int compare(ILibWidget o1, ILibWidget o2) {
                return Integer.compare(o1.getPriority().ordinal(), o2.getPriority().ordinal());
            }
        };
    }
}
