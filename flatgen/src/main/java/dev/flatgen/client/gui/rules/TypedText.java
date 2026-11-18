package dev.flatgen.client.gui.rules;

import java.util.function.Predicate;

/**
 * The text a player types into a field: a character is taken when the text it makes is one the field accepts and not
 * longer than its most, and Backspace takes the last character off.
 */
public final class TypedText {
    private final Predicate<String> accepts;
    private final int most;
    private String text = "";

    /** Text the field accepts as it grows, at most this many characters. */
    public TypedText(Predicate<String> accepts, int most) {
        this.accepts = accepts;
        this.most = most;
    }

    /** The text typed so far. */
    public String text() {
        return text;
    }

    /** Starts the text afresh, empty. */
    public void clear() {
        text = "";
    }

    /** Types a character; whether it was taken. */
    public boolean type(char character) {
        String longer = text + character;
        if (longer.length() > most || !accepts.test(longer)) {
            return false;
        }
        text = longer;
        return true;
    }

    /** Takes the last character off, if there is one. */
    public void backspace() {
        text = text.isEmpty() ? text : text.substring(0, text.length() - 1);
    }
}
