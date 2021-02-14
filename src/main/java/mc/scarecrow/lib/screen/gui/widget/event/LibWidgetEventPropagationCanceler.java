package mc.scarecrow.lib.screen.gui.widget.event;

import java.util.concurrent.CancellationException;

public final class LibWidgetEventPropagationCanceler extends CancellationException {

    public LibWidgetEventPropagationCanceler(String message) {
        super(message);
    }

    public static void cancelPropagation() {
        throw new LibWidgetEventPropagationCanceler("Event propagation canceled by widget");
    }
}
