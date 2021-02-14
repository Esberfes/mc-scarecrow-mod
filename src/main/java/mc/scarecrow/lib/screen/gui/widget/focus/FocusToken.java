package mc.scarecrow.lib.screen.gui.widget.focus;

import mc.scarecrow.lib.math.LibVector2D;
import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.widget.event.ILibWidgetFocusable;

import java.util.Objects;
import java.util.function.Supplier;

public class FocusToken {

    private ILibWidgetFocusable owner;
    private final Supplier<LibVector2D> positionSupplier;

    public FocusToken(Supplier<LibVector2D> positionSupplier) {
        this.positionSupplier = positionSupplier;
    }

    public synchronized void claimOrReleaseIfNeeded(ILibWidgetFocusable candidate, LibVectorBox dimensions) {
        // Should be the owner or the new owner
        if (dimensions.isCollisionTo(this.positionSupplier.get())) {
            if (!Objects.equals(candidate, this.owner)) {
                forceClaim(candidate);
            }
            // Already owner, nothing to do..
        } else {
            forceRelease();
        }
    }

    public synchronized void claimIfOnMe(ILibWidgetFocusable focusable, LibVectorBox dimensions) {
        if (dimensions.isCollisionTo(this.positionSupplier.get())) {
            if (owner != null)
                owner.onFocusChange();

            this.owner = focusable;
            this.owner.onFocusClaimed();
        }
    }

    public synchronized void releaseIfOwn(ILibWidgetFocusable candidate) {
        if (Objects.equals(candidate, this.owner)) {
            this.owner.onFocusChange();
            this.owner = null;
        }
    }

    /**
     * Maybe dangerous... TODO think about this
     *
     * @param focusable
     */
    public synchronized void forceClaim(ILibWidgetFocusable focusable) {
        forceRelease();

        this.owner = focusable;
        this.owner.onFocusClaimed();
    }

    /**
     * Maybe dangerous... TODO think about this
     *
     */
    public synchronized void forceRelease() {
        if (owner != null)
            owner.onFocusChange();

        this.owner = null;
    }
}
