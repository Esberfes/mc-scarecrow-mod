package mc.scarecrow.lib.screen.gui.widget.event;

import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.screen.gui.widget.base.ILibWidget;
import mc.scarecrow.lib.screen.gui.widget.focus.FocusToken;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LibWidgetEventPropagator implements ILibWidgetEventListener {

    Supplier<List<ILibWidget>> widgetSupplier;

    public LibWidgetEventPropagator(Supplier<List<ILibWidget>> widgetSupplier) {
        this.widgetSupplier = widgetSupplier;
    }

    @Override
    public void onClick(LibVector2D position, int button) {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    if (widget.getDimensionsBox().isCollisionTo(position))
                        widget.onClick(position, button);
                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not a error, this occurs when a widget cancel propagation for some reason
                    break;
                }
            }
        });
    }

    @Override
    public void onClickRelease(LibVector2D position, int button) {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    widget.onClickRelease(position, button);

                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not cancellable
                }
            }
        });
    }

    @Override
    public void onHover(LibVector2D vector2D) {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    if (widget.getDimensionsBox().isCollisionTo(vector2D))
                        widget.onHover(vector2D);
                    else
                        widget.onHoverOut();
                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not a error, this occurs when a widget cancel propagation for some reason
                    break;
                }
            }
        });
    }

    @Override
    public void onHoverOut() {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    widget.onHoverOut();
                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not cancellable
                }
            }
        });
    }

    @Override
    public void onMouseScrolled(LibVector2D position, double delta) {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    widget.onMouseScrolled(position, delta);
                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not a error, this occurs when a widget cancel propagation for some reason
                    break;
                }
            }
        });
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    widget.keyPressed(keyCode, scanCode, modifiers);
                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not cancellable
                }
            }
        });
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    widget.charTyped(codePoint, modifiers);
                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not cancellable
                }
            }
        });
    }

    @Override
    public void onTokenReceived(FocusToken token) {
        CompletableFuture.runAsync(() -> {
            for (ILibWidget widget : widgetSupplier.get().stream()
                    .sorted(ILibWidgetEventListener.getPriorityComparator())
                    .collect(Collectors.toList())) {
                try {
                    widget.onTokenReceived(token);
                } catch (LibWidgetEventPropagationCanceler ignore) {
                    // Not cancellable TODO sure?
                }
            }
        });
    }
}
