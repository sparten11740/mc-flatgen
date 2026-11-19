package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;

/**
 * A part of a Flatgen screen: it draws through a {@link Canvas} and hears the mouse in screen coordinates. A screen
 * hands a click to its widgets topmost first until one takes it; that widget then hears the drag and the release. A
 * widget that can take the keyboard has it after it took a click, until another widget takes a click. A control can
 * also have the keyboard's focus, moved to it with Tab and the arrows; it then hears the keys, Enter using it.
 */
public interface Widget {
    /** Draws the widget, knowing where the mouse is. */
    void draw(Canvas canvas, int mouseX, int mouseY);

    /** A mouse button pressed at a point; whether the widget took it. */
    default boolean click(double x, double y, int button, Modifiers modifiers) {
        return false;
    }

    /** The mouse moved to a point while the button it took the click of is held. */
    default void drag(double x, double y) {
    }

    /** The button it took the click of let go at a point. */
    default void release(double x, double y) {
    }

    /** The wheel turned by an amount at a point, positive away from the player; whether the widget took it. */
    default boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        return false;
    }

    /** Whether the widget takes the keyboard after it took a click. */
    default boolean takesKeyboard() {
        return false;
    }

    /** Whether the widget has the keyboard now; told again with each click it takes while it keeps it. */
    default void keyboard(boolean held) {
    }

    /** A key pressed while the widget has the keyboard, by the game's key code; whether the widget took it. */
    default boolean key(int keyCode, Modifiers modifiers) {
        return false;
    }

    /** A character typed while the widget has the keyboard; whether the widget took it. */
    default boolean type(char character) {
        return false;
    }

    /**
     * The controls the keyboard's focus can move to, in order: the widget itself for a control, its widgets' for a
     * group, none by default.
     */
    default List<Widget> focusables() {
        return List.of();
    }

    /** Where the focus ring is drawn while the widget has the focus; none for a widget that never has it. */
    default Optional<Rect> focusArea() {
        return Optional.empty();
    }

    /** What the widget says to the game's narrator while it has the focus; nothing by default. */
    default Optional<Narration> narration() {
        return Optional.empty();
    }

    /** What the widget at a point says to the narrator, the mouse being there; a group asks its widgets. */
    default Optional<Narration> narrationAt(double x, double y) {
        return focusArea().filter(area -> area.contains(x, y)).flatMap(area -> narration());
    }

    /** Brings this part of the screen into view, a group that scrolls scrolling to it; nothing by default. */
    default void reveal(Rect area) {
    }

    /**
     * What the widget says of itself to tools that drive the screen, or a group of each of its widgets; nothing for a
     * widget they need not find.
     */
    default List<WidgetDescription> describe() {
        return List.of();
    }
}
