package mc.scarecrow.lib.screen.gui.widget.event.observer;

public interface LibObserver<T> {
    void onChange(LibObservable<T> libObservable, T newValue);
}
