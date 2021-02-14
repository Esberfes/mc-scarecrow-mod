package mc.scarecrow.lib.screen.gui.widget.event;

import mc.scarecrow.lib.screen.gui.widget.focus.FocusToken;

import java.util.Observable;
import java.util.Observer;

public interface ILibWidgetFocusable extends Observer {

    default void onFocusClaimed() {
    }

    default void onFocusChange() {
    }

    default void onTokenReceived(FocusToken token) {
    }

    @Override
    default void update(Observable observable, Object arg) {
    }
}
