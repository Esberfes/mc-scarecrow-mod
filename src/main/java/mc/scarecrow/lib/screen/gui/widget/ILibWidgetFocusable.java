package mc.scarecrow.lib.screen.gui.widget;

public interface ILibWidgetFocusable {

    default void onFocusClaimed() {
    }

    ;

    default void onFocusChange() {
    }

    ;

    default void onTokenReceived(FocusToken token) {
    }
}
