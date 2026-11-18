package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** How far the wheel scrolls a widget. */
class ScrollingTest {
    @Test
    void scrollsOneStepATurnDownForATurnTowardsThePlayer() {
        assertEquals(16, Scrolling.scrolled(0, -1, 16, 100));
        assertEquals(16, Scrolling.scrolled(0, -3.5, 16, 100));
        assertEquals(4, Scrolling.scrolled(20, 0.2, 16, 100));
    }

    @Test
    void keepsBetweenTheTopAndAsMuchAsIsHidden() {
        assertEquals(0, Scrolling.scrolled(10, 1, 16, 100));
        assertEquals(100, Scrolling.scrolled(90, -1, 16, 100));
        assertEquals(0, Scrolling.scrolled(30, -1, 16, -5));
    }
}
