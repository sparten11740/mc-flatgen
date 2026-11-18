package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import org.junit.jupiter.api.Test;

/** What flatgen:blocks reports of the block picker's grid. */
class BlockRowsTest {
    @Test
    void listsOnlyTheSlotsWhollyInSight() {
        Rect sight = new Rect(12, 48, 400, 160);
        List<ReportRow> rows = BlockRows.of(List.of(
                new BlockRows.Slot("minecraft:stone", new Rect(12, 30, 18, 18)),
                new BlockRows.Slot("minecraft:dirt", new Rect(12, 64, 18, 18)),
                new BlockRows.Slot("minecraft:sand", new Rect(30, 200, 18, 18))), sight);
        assertEquals(1, rows.size());
        assertEquals("b0", rows.get(0).key());
        assertEquals("minecraft:dirt", rows.get(0).fields().get("block"));
        assertEquals("12,64,18,18", rows.get(0).fields().get("bounds"));
    }
}
