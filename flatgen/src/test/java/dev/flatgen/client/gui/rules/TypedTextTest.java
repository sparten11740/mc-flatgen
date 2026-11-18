package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Text typed into a field. */
class TypedTextTest {
    @Test
    void takesWhatTheFieldAcceptsUpToItsLength() {
        TypedText number = new TypedText(text -> text.matches("-?\\d*"), 3);
        assertTrue(number.type('-'));
        assertTrue(number.type('4'));
        assertFalse(number.type('-'));
        assertFalse(number.type('x'));
        assertTrue(number.type('2'));
        assertFalse(number.type('1'));
        assertEquals("-42", number.text());
    }

    @Test
    void takesTheLastCharacterOffAndStartsAfresh() {
        TypedText text = new TypedText(typed -> true, 10);
        text.type('a');
        text.type('b');
        text.backspace();
        assertEquals("a", text.text());
        text.clear();
        text.backspace();
        assertEquals("", text.text());
    }
}
