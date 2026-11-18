package dev.flatgen.compat.blockpilot.rules;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What {@code flatgen:widgets} reports: a row for each of a Flatgen screen's widgets that says what it is, keyed
 * {@code f<n>} in the screen's order, with its kind, label, place, whether it can be used and what it holds.
 */
public final class WidgetRows {
    private WidgetRows() {
    }

    /** The rows for these widgets. */
    public static List<ReportRow> of(List<WidgetDescription> widgets) {
        List<ReportRow> rows = new ArrayList<>();
        for (int index = 0; index < widgets.size(); index++) {
            WidgetDescription widget = widgets.get(index);
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("kind", widget.kind());
            fields.put("label", widget.label());
            fields.put("bounds", bounds(widget.bounds()));
            fields.put("active", widget.active());
            fields.put("value", widget.value());
            rows.add(new ReportRow("f" + index, fields));
        }
        return rows;
    }

    /** A rectangle as {@code x,y,width,height}, as Blockpilot writes bounds. */
    static String bounds(Rect rect) {
        return rect.x() + "," + rect.y() + "," + rect.width() + "," + rect.height();
    }
}
