package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** What flatgen:column reports of each layer. */
class ColumnRowsTest {
    private static final List<Rect> BOUNDS = List.of(
            new Rect(12, 216, 48, 8), new Rect(12, 40, 48, 176), new Rect(12, 32, 48, 8), new Rect(12, 24, 48, 8));

    @Test
    void listsTheLayersTopFirstKeyedFromTheBottom() {
        List<ColumnRows.Row> rows = ColumnRows.of(TestPresets.TALL_FLAT, -64, BOUNDS, 3);
        assertEquals(List.of("l3", "l2", "l1", "l0"), rows.stream().map(ColumnRows.Row::key).toList());
    }

    @Test
    void saysEachLayersBlockYsAndWhereItIsDrawn() {
        ColumnRows.Row stone = ColumnRows.of(TestPresets.TALL_FLAT, -64, BOUNDS, 3).get(2);
        assertEquals("minecraft:stone", stone.fields().get("block"));
        assertEquals(100, stone.fields().get("height"));
        assertEquals(-63, stone.fields().get("from_y"));
        assertEquals(36, stone.fields().get("to_y"));
        assertEquals("12,40,48,176", stone.fields().get("bounds"));
        assertEquals(false, stone.fields().get("selected"));
    }

    @Test
    void marksTheSelectedLayer() {
        ColumnRows.Row grass = ColumnRows.of(TestPresets.TALL_FLAT, -64, BOUNDS, 3).get(0);
        assertEquals(true, grass.fields().get("selected"));
    }
}
