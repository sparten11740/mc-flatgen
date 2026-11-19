package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Flatgen GUI pixels: whole window pixels, as many as keep the screen at least 640 by 360 of them. */
class LayoutScaleTest {
    @Test
    void aFullHdWindowIsTheSmallestLayoutAtThreePixels() {
        assertEquals(new LayoutScale(3, 640, 360, 0.75F), LayoutScale.of(1920, 1080, 4));
    }

    @Test
    void aWindowBetweenWholePixelsTakesTheWholePixelBelow() {
        assertEquals(new LayoutScale(2, 854, 480, 0.5F), LayoutScale.of(1708, 960, 4));
    }

    @Test
    void aTallerWindowKeepsTheWidthAndGainsHeight() {
        assertEquals(new LayoutScale(4, 756, 491, 2F), LayoutScale.of(3024, 1964, 2));
    }

    @Test
    void aWindowSmallerThanTheLayoutDrawsAtOnePixel() {
        assertEquals(new LayoutScale(1, 427, 240, 1F), LayoutScale.of(427, 240, 1));
    }

    @Test
    void convertsBetweenTheScreensPixelsAndTheGames() {
        LayoutScale scale = LayoutScale.of(1920, 1080, 4);
        assertEquals(new Rect(75, 15, 30, 8), scale.toGame(new Rect(100, 20, 40, 10)));
        assertEquals(100, scale.fromGame(75), 1e-9);
    }
}
