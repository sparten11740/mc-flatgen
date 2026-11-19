package dev.flatgen.layers.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

/** Where the editor's column draws each layer: proportional, never thinner than the minimum, scrolling when it must. */
class ColumnLayoutTest {
    private static final int MINIMUM = 8;
    private static final int FLOOR = -64;
    /** Bedrock 1, stone 100, dirt 2, grass 1: the test instance's usual stack. */
    private static final List<Integer> TALL_FLAT = List.of(1, 100, 2, 1);

    @Test
    void oneLayerFillsTheColumn() {
        ColumnLayout layout = ColumnLayout.of(List.of(3), 200, MINIMUM);
        assertEquals(List.of(new ColumnLayout.Span(0, 200)), layout.spans());
    }

    @Test
    void keepsThinLayersAtTheMinimumAndGivesTheRestToTheDeepOne() {
        ColumnLayout layout = ColumnLayout.of(TALL_FLAT, 200, MINIMUM);
        assertEquals(List.of(8, 176, 8, 8), heights(layout));
        assertFillsExactly(layout, 200);
    }

    @Test
    void drawsTheBottomLayerLowest() {
        ColumnLayout layout = ColumnLayout.of(TALL_FLAT, 200, MINIMUM);
        assertEquals(200, layout.spans().get(0).bottom());
        assertEquals(0, layout.spans().get(3).top());
    }

    @Test
    void sharesInProportionWhereNoLayerIsTooThin() {
        ColumnLayout layout = ColumnLayout.of(List.of(1, 2, 1), 200, MINIMUM);
        assertEquals(List.of(50, 100, 50), heights(layout));
    }

    @Test
    void roundsThirtyLayersToTheWholeColumnWithNoneTooThin() {
        List<Integer> layers = new ArrayList<>();
        for (int index = 0; index < 30; index++) {
            layers.add(index % 3 == 0 ? 7 : 1);
        }
        ColumnLayout layout = ColumnLayout.of(layers, 301, MINIMUM);
        assertFillsExactly(layout, 301);
        assertTrue(heights(layout).stream().allMatch(height -> height >= MINIMUM));
    }

    @Test
    void growsTallerThanItsSpaceWhenEvenTheMinimumsDoNotFit() {
        ColumnLayout layout = ColumnLayout.of(Collections.nCopies(300, 1), 200, MINIMUM);
        assertEquals(2400, layout.contentHeight());
        assertTrue(heights(layout).stream().allMatch(height -> height == MINIMUM));
        assertFillsExactly(layout, 2400);
    }

    @Test
    void findsTheLayerAtAPixel() {
        ColumnLayout layout = ColumnLayout.of(TALL_FLAT, 200, MINIMUM);
        assertEquals(OptionalInt.of(3), layout.layerAt(0));
        assertEquals(OptionalInt.of(1), layout.layerAt(100));
        assertEquals(OptionalInt.of(0), layout.layerAt(199));
        assertEquals(OptionalInt.empty(), layout.layerAt(200));
    }

    @Test
    void placesLinesBetweenBlocksInProportionWithinTheirLayer() {
        ColumnLayout layout = ColumnLayout.of(TALL_FLAT, 200, MINIMUM);
        assertEquals(OptionalInt.of(200), layout.pixelOfLine(FLOOR, FLOOR, TALL_FLAT));
        assertEquals(OptionalInt.of(192), layout.pixelOfLine(FLOOR + 1, FLOOR, TALL_FLAT));
        assertEquals(OptionalInt.of(104), layout.pixelOfLine(FLOOR + 51, FLOOR, TALL_FLAT));
        assertEquals(OptionalInt.of(0), layout.pixelOfLine(40, FLOOR, TALL_FLAT));
        assertEquals(OptionalInt.empty(), layout.pixelOfLine(80, FLOOR, TALL_FLAT));
        assertEquals(OptionalInt.empty(), layout.pixelOfLine(FLOOR - 1, FLOOR, TALL_FLAT));
    }

    @Test
    void laysOutAnEmptyStackAsAnEmptyColumn() {
        ColumnLayout layout = ColumnLayout.of(List.of(), 200, MINIMUM);
        assertEquals(List.of(), layout.spans());
        assertEquals(OptionalInt.empty(), layout.layerAt(10));
    }

    private static List<Integer> heights(ColumnLayout layout) {
        List<Integer> heights = new ArrayList<>();
        layout.spans().forEach(span -> heights.add(span.height()));
        return heights;
    }

    private static void assertFillsExactly(ColumnLayout layout, int height) {
        assertEquals(height, heights(layout).stream().mapToInt(Integer::intValue).sum());
        for (int index = 1; index < layout.spans().size(); index++) {
            assertEquals(layout.spans().get(index - 1).top(), layout.spans().get(index).bottom());
        }
    }

    @Test
    void drawsAThinStackAsItsShareOfTheWorldButEachLayerAtLeastItsSmallestShare() {
        assertEquals(64, ColumnLayout.stackSpace(400, 16, 4, 384, 4, 16));
        assertEquals(200, ColumnLayout.stackSpace(400, 16, 192, 384, 2, 16));
    }

    @Test
    void keepsHeadroomAboveAStackThatWouldFillTheColumn() {
        assertEquals(384, ColumnLayout.stackSpace(400, 16, 384, 384, 3, 16));
    }
}
