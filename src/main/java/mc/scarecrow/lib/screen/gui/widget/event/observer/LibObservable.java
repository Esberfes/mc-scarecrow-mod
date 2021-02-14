package mc.scarecrow.lib.screen.gui.widget.event.observer;


import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class LibObservable<T> extends Observable {

    private final AtomicReference<T> reference;

    private LibObservable(T reference) {
        this.reference = new AtomicReference<>(reference);
    }

    public T get() {
        return reference.get();
    }

    public void set(T value) {
        this.reference.set(value);
        setChanged();
        notifyObservers(value);
    }

    public void set(Consumer<T> setter) {
        setter.accept(this.get());
        setChanged();
        notifyObservers(this.get());
    }

    public static class Builder<T> {

        private LibObservable<T> libObservable;
        private final List<LibObserver<T>> observers;
        private T initialValue;

        public Builder() {
            this.observers = new LinkedList<>();
        }

        public Builder<T> value(T initialValue) {
            this.initialValue = initialValue;

            return this;
        }

        public Builder<T> observer(LibObserver<T> observer) {
            this.observers.add(observer);

            return this;
        }

        public LibObservable<T> build() {
            this.libObservable = new LibObservable<T>(initialValue);
            this.observers.forEach(ov -> this.libObservable.addObserver((o, arg) -> {
                        if (this.libObservable.get().getClass().isAssignableFrom(arg.getClass())) {
                            ov.onChange(this.libObservable, (T) arg);
                        }
                    }
            ));
            return this.libObservable;
        }
    }
}
