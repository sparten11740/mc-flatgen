package dev.flatgen.compat.blockpilot.rules;

import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What {@code flatgen:picker} reports: a row for each card of the open picker's grid in sight, keyed {@code c<n>} in
 * the grid's order, with its entry's id and where it is drawn.
 */
public final class PickerRows {
    private PickerRows() {
    }

    /**
     * A card of the grid.
     *
     * @param id    its entry's id, a block's or a biome's
     * @param place where it is drawn
     */
    public record Card(String id, Rect place) {
    }

    /** The rows for the cards wholly within this rectangle, the grid's sight. */
    public static List<ReportRow> of(List<Card> cards, Rect sight) {
        List<ReportRow> rows = new ArrayList<>();
        for (Card card : cards) {
            if (card.place().y() >= sight.y() && card.place().bottom() <= sight.bottom()) {
                Map<String, Object> fields = new LinkedHashMap<>();
                fields.put("id", card.id());
                fields.put("bounds", WidgetRows.bounds(card.place()));
                rows.add(new ReportRow("c" + rows.size(), fields));
            }
        }
        return rows;
    }
}
