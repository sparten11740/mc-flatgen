package dev.flatgen.client.gui.rules;

/** The mouse buttons widgets tell apart, as Flatgen numbers them: the game's own numbers up to 26.2. */
public final class MouseButtons {
    /** The left button: pick, press, drag. */
    public static final int LEFT = 0;
    /** The right button: star. */
    public static final int RIGHT = 1;
    /** Any other button, which no widget answers. */
    public static final int OTHER = 2;

    private MouseButtons() {
    }
}
