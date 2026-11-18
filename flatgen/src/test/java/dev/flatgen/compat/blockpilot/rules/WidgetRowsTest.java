package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import org.junit.jupiter.api.Test;

/** What flatgen:widgets reports of a Flatgen screen's widgets. */
class WidgetRowsTest {
    @Test
    void keysEachWidgetInTheScreensOrderWithWhatItSaysOfItself() {
        List<ReportRow> rows = WidgetRows.of(List.of(
                new WidgetDescription("button", "+", new Rect(166, 64, 20, 20), false, ""),
                new WidgetDescription("number", "height", new Rect(122, 64, 40, 20), true, "100")));
        assertEquals("f0", rows.get(0).key());
        assertEquals("button", rows.get(0).fields().get("kind"));
        assertEquals("166,64,20,20", rows.get(0).fields().get("bounds"));
        assertEquals(false, rows.get(0).fields().get("active"));
        assertEquals("f1", rows.get(1).key());
        assertEquals("100", rows.get(1).fields().get("value"));
    }
}
