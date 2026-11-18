package dev.flatgen.client.gui;

/**
 * A part of a Flatgen screen: it draws through a {@link Canvas} and hears the mouse in screen coordinates. A screen
 * hands input to its widgets topmost first, until one takes it.
 */
public interface Widget {
    /** Draws the widget, knowing where the mouse is. */
    void draw(Canvas canvas, int mouseX, int mouseY);

    /** A mouse button pressed at a point; whether the widget took it. */
    default boolean click(double x, double y, int button) {
        return false;
    }

    /** The wheel turned by an amount at a point, positive away from the player; whether the widget took it. */
    default boolean scroll(double x, double y, double amount) {
        return false;
    }
}
