package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** What flatgen:column reports of each layer. */
class ColumnRowsTest {
    /** The tall flat preset with its top layer, the grass, selected. */
    private static final EditorState TOP_SELECTED = new EditorState(TestPresets.TALL_FLAT, Dimension.OVERWORLD, 3);
    private static final List<Rect> BOUNDS = List.of(
            new Rect(12, 216, 48, 8), new Rect(12, 40, 48, 176), new Rect(12, 32, 48, 8), new Rect(12, 24, 48, 8));

    @Test
    void listsTheLayersTopFirstKeyedFromTheBottom() {
        List<ReportRow> rows = ColumnRows.of(TOP_SELECTED, -64, BOUNDS);
        assertEquals(List.of("l3", "l2", "l1", "l0"), rows.stream().map(ReportRow::key).toList());
    }

    @Test
    void saysEachLayersBlockYsAndWhereItIsDrawn() {
        ReportRow stone = ColumnRows.of(TOP_SELECTED, -64, BOUNDS).get(2);
        assertEquals("minecraft:stone", stone.fields().get("block"));
        assertEquals(100, stone.fields().get("height"));
        assertEquals(-63, stone.fields().get("from_y"));
        assertEquals(36, stone.fields().get("to_y"));
        assertEquals("12,40,48,176", stone.fields().get("bounds"));
        assertEquals(false, stone.fields().get("selected"));
    }

    @Test
    void marksTheSelectedLayer() {
        ReportRow grass = ColumnRows.of(TOP_SELECTED, -64, BOUNDS).get(0);
        assertEquals(true, grass.fields().get("selected"));
    }
}
