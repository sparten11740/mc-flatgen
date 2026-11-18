package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Rows of widgets that wrap within an area's width. */
class FlowLayoutTest {
    @Test
    void placesWidgetsLeftToRightAndWrapsWhenOneDoesNotFit() {
        FlowLayout flow = new FlowLayout(new Rect(10, 20, 100, 200), 20, 4);
        assertEquals(new Rect(10, 20, 50, 20), flow.next(50));
        assertEquals(new Rect(64, 20, 40, 20), flow.next(40));
        assertEquals(new Rect(10, 44, 30, 20), flow.next(30));
        assertEquals(64, flow.bottom());
    }

    @Test
    void putsAWidgetWiderThanTheAreaOnARowOfItsOwn() {
        FlowLayout flow = new FlowLayout(new Rect(0, 0, 100, 200), 20, 4);
        assertEquals(new Rect(0, 0, 150, 20), flow.next(150));
        assertEquals(new Rect(0, 24, 10, 20), flow.next(10));
    }

    @Test
    void startsLinesAndNewRowsBelowWhatWasPlaced() {
        FlowLayout flow = new FlowLayout(new Rect(0, 0, 100, 200), 20, 4);
        assertEquals(new Rect(0, 0, 100, 9), flow.line(9));
        flow.next(30);
        flow.newRow();
        assertEquals(new Rect(0, 37, 30, 20), flow.next(30));
        assertEquals(new Rect(0, 61, 100, 9), flow.line(9));
        assertEquals(74, flow.bottom());
    }
}
