package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Equal slots one below another: where each is, and which one a height falls on. */
class ListSlotsTest {
    private static final ListSlots THREE = new ListSlots(new Rect(10, 100, 200, 0), 3, 35, 4);

    @Test
    void placesEachSlotBelowTheOneBefore() {
        assertEquals(new Rect(10, 178, 200, 35), THREE.at(2));
        assertEquals(213, THREE.bottom());
    }

    @Test
    void findsTheSlotAtAHeightAndTheNearestEndBeyondTheList() {
        assertEquals(1, THREE.placeAt(140));
        assertEquals(0, THREE.placeAt(20));
        assertEquals(2, THREE.placeAt(900));
    }

    @Test
    void findsNoSlotInAnEmptyList() {
        assertEquals(-1, new ListSlots(new Rect(0, 0, 10, 0), 0, 35, 4).placeAt(5));
    }
}
