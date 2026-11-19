package dev.flatgen.client.gui.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** A screen's header, body and footer, and the footer's actions at its right. */
class PageFrameTest {
    @Test
    void putsTheBodyBetweenTheHeaderAndTheFooter() {
        assertEquals(new Rect(13, 43, 614, 266), new PageFrame(640, 360).body());
    }

    @Test
    void endsTheActionsAtTheFootersRightEdge() {
        assertEquals(List.of(new Rect(527, 330, 46, 19), new Rect(577, 330, 50, 19)),
                new PageFrame(640, 360).actions(46, 50));
    }
}
