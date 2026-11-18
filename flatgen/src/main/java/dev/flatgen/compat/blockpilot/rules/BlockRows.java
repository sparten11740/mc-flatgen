package dev.flatgen.compat.blockpilot.rules;

import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What {@code flatgen:blocks} reports: a row for each slot of the block picker's grid in sight, keyed {@code b<n>} in
 * the grid's order, with its block and where it is drawn.
 */
public final class BlockRows {
    private BlockRows() {
    }

    /**
     * A slot of the grid.
     *
     * @param block the block's id
     * @param place where its slot is drawn
     */
    public record Slot(String block, Rect place) {
    }

    /** The rows for the slots within this rectangle, the grid's sight. */
    public static List<ReportRow> of(List<Slot> slots, Rect sight) {
        List<ReportRow> rows = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot.place().y() >= sight.y() && slot.place().bottom() <= sight.bottom()) {
                Map<String, Object> fields = new LinkedHashMap<>();
                fields.put("block", slot.block());
                fields.put("bounds", WidgetRows.bounds(slot.place()));
                rows.add(new ReportRow("b" + rows.size(), fields));
            }
        }
        return rows;
    }
}
