package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tab between areas, skipping those without controls and going round; arrows within one; places kept after layout. */
class FocusOrderTest {
    /** A screen of a column, a picture with no controls, a panel of five and a footer of three. */
    private static final List<Integer> SCREEN = List.of(1, 0, 5, 3);

    @Test
    void tabsToTheNextAreaWithControlsGoingRound() {
        assertEquals(Optional.of(new FocusOrder.Place(0, 0)), FocusOrder.tab(SCREEN, Optional.empty(), true));
        assertEquals(Optional.of(new FocusOrder.Place(2, 0)),
                FocusOrder.tab(SCREEN, Optional.of(new FocusOrder.Place(0, 0)), true));
        assertEquals(Optional.of(new FocusOrder.Place(0, 0)),
                FocusOrder.tab(SCREEN, Optional.of(new FocusOrder.Place(3, 2)), true));
    }

    @Test
    void tabsBackFromTheEnd() {
        assertEquals(Optional.of(new FocusOrder.Place(3, 0)), FocusOrder.tab(SCREEN, Optional.empty(), false));
        assertEquals(Optional.of(new FocusOrder.Place(0, 0)),
                FocusOrder.tab(SCREEN, Optional.of(new FocusOrder.Place(2, 4)), false));
        assertEquals(Optional.empty(), FocusOrder.tab(List.of(0, 0), Optional.empty(), true));
    }

    @Test
    void stepsWithinAnAreaAndStopsAtItsEnds() {
        assertEquals(Optional.of(new FocusOrder.Place(2, 1)),
                FocusOrder.step(SCREEN, new FocusOrder.Place(2, 0), true));
        assertEquals(Optional.empty(), FocusOrder.step(SCREEN, new FocusOrder.Place(2, 4), true));
        assertEquals(Optional.empty(), FocusOrder.step(SCREEN, new FocusOrder.Place(2, 0), false));
    }

    @Test
    void keepsAPlaceWithinTheAreasAsTheyAreNow() {
        assertEquals(Optional.of(new FocusOrder.Place(2, 2)), FocusOrder.within(List.of(1, 0, 3),
                new FocusOrder.Place(2, 4)));
        assertEquals(Optional.empty(), FocusOrder.within(List.of(1, 0), new FocusOrder.Place(1, 0)));
        assertEquals(Optional.empty(), FocusOrder.within(List.of(1), new FocusOrder.Place(3, 0)));
    }
}
