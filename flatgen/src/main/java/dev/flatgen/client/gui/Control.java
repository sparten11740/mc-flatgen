package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.UiSounds;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * A widget the player uses, a button, a switch, a field or a slider, in a rectangle of its own: the keyboard's focus
 * can move to it, the ring drawn round its rectangle.
 */
interface Control extends Widget {
    /** Where it is on the screen. */
    Rect bounds();

    @Override
    default List<Widget> focusables() {
        return List.of(this);
    }

    @Override
    default Optional<Rect> focusArea() {
        return Optional.of(bounds());
    }

    /**
     * Uses a button or a switch, by a click or by a key while it has the focus: its sound and its action, if it can be
     * used now; taken all the same when it cannot.
     */
    static boolean use(BooleanSupplier usable, Runnable action) {
        if (usable.getAsBoolean()) {
            UiSounds.click();
            action.run();
        }
        return true;
    }

    /** A left click inside a control uses it, as {@link #use} does; any other click it leaves be. */
    static boolean clicked(Rect bounds, double x, double y, int button, BooleanSupplier usable, Runnable action) {
        return button == MouseButtons.LEFT && bounds.contains(x, y) && use(usable, action);
    }

    /** Enter or Space uses a control while it has the focus, as a click does; any other key it leaves be. */
    static boolean keyed(int keyCode, BooleanSupplier usable, Runnable action) {
        return Keys.uses(keyCode) && use(usable, action);
    }
}
