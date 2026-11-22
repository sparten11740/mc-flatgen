package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** A rectangle of the screen: the part of it inside another. */
class RectTest {
    private static final Rect PICTURE = new Rect(10, 10, 16, 16);

    @Test
    void keepsThePartInsideAndAllOfOneWhollyInside() {
        assertEquals(new Rect(10, 10, 6, 16), new Rect(2, 4, 14, 30).within(PICTURE));
        assertEquals(new Rect(12, 12, 4, 4), new Rect(12, 12, 4, 4).within(PICTURE));
    }

    @Test
    void leavesNothingOfOneOutside() {
        Rect outside = new Rect(40, 12, 8, 8).within(PICTURE);
        assertEquals(0, outside.width());
    }
}
