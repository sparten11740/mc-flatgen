package dev.flatgen.compat.blockpilot.rules;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.preset.rules.Layer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What {@code flatgen:column} reports: a row for each layer, top first as the column shows them, keyed {@code l<n>}
 * with {@code n} counted from the bottom, with the layer's block, height, Ys and where it is drawn, so {@code click}
 * and {@code drag} can aim at it.
 */
public final class ColumnRows {
    private ColumnRows() {
    }

    /** The rows for the edited stack's layers, in a world whose floor is at this Y, drawn in these rectangles. */
    public static List<ReportRow> of(EditorState state, int floor, List<Rect> bounds) {
        List<ReportRow> rows = new ArrayList<>();
        int bottomY = floor;
        for (int index = 0; index < state.layers().size(); index++) {
            Layer layer = state.layers().get(index);
            rows.add(0, row(index, layer, bottomY, bounds.get(index), index == state.selected()));
            bottomY += layer.height();
        }
        return rows;
    }

    private static ReportRow row(int index, Layer layer, int bottomY, Rect bounds, boolean selected) {
        int topY = bottomY + layer.height() - 1;
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("block", layer.block());
        fields.put("height", layer.height());
        fields.put("from_y", bottomY);
        fields.put("to_y", topY);
        fields.put("bounds", WidgetRows.bounds(bounds));
        fields.put("selected", selected);
        return new ReportRow("l" + index, fields);
    }
}
