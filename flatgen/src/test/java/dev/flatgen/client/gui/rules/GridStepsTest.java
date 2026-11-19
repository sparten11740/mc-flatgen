package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

/** Arrows in a grid: across within the order, up and down to the nearest row's nearest card, none past the edge. */
class GridStepsTest {
    /** Three cards in a row, then a short row of one under a heading, then two more. */
    private static final List<Rect> CARDS = List.of(new Rect(0, 0, 10, 10), new Rect(10, 0, 10, 10),
            new Rect(20, 0, 10, 10), new Rect(0, 22, 10, 10), new Rect(0, 44, 10, 10), new Rect(10, 44, 10, 10));

    @Test
    void movesAcrossInTheGridsOrderAndStopsAtItsEnds() {
        assertEquals(OptionalInt.of(3), GridSteps.next(CARDS, 2, 1, 0));
        assertEquals(OptionalInt.of(0), GridSteps.next(CARDS, 1, -1, 0));
        assertEquals(OptionalInt.empty(), GridSteps.next(CARDS, 5, 1, 0));
        assertEquals(OptionalInt.empty(), GridSteps.next(CARDS, 0, -1, 0));
    }

    @Test
    void movesDownAndUpToTheNearestRowsNearestCard() {
        assertEquals(OptionalInt.of(3), GridSteps.next(CARDS, 2, 0, 1));
        assertEquals(OptionalInt.of(4), GridSteps.next(CARDS, 3, 0, 1));
        assertEquals(OptionalInt.of(3), GridSteps.next(CARDS, 5, 0, -1));
        assertEquals(OptionalInt.of(0), GridSteps.next(CARDS, 3, 0, -1));
        assertEquals(OptionalInt.empty(), GridSteps.next(CARDS, 4, 0, 1));
        assertEquals(OptionalInt.empty(), GridSteps.next(CARDS, 1, 0, -1));
    }
}
