package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import org.junit.jupiter.api.Test;

/** What flatgen:picker reports of the open picker's grid. */
class PickerRowsTest {
    @Test
    void listsOnlyTheCardsWhollyInSight() {
        Rect sight = new Rect(12, 48, 400, 160);
        List<ReportRow> rows = PickerRows.of(List.of(
                new PickerRows.Card("minecraft:stone", new Rect(12, 30, 18, 18)),
                new PickerRows.Card("minecraft:dirt", new Rect(12, 64, 18, 18)),
                new PickerRows.Card("minecraft:sand", new Rect(30, 200, 18, 18))), sight);
        assertEquals(1, rows.size());
        assertEquals("c0", rows.get(0).key());
        assertEquals("minecraft:dirt", rows.get(0).fields().get("id"));
        assertEquals("12,64,18,18", rows.get(0).fields().get("bounds"));
    }
}
