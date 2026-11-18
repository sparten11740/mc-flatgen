package dev.flatgen.client.gui.rules;

/**
 * The modifier keys held while the mouse or a key is used. On a Mac, control is the Command key, as the game has it.
 *
 * @param shift   whether Shift is held
 * @param control whether Control (Command on a Mac) is held
 * @param alt     whether Alt (Option on a Mac) is held
 */
public record Modifiers(boolean shift, boolean control, boolean alt) {
    /** No modifier held. */
    public static final Modifiers NONE = new Modifiers(false, false, false);
}
