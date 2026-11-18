package dev.flatgen.facts.client;

import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.layers.client.ColumnWidget;
import java.util.List;
import java.util.function.Supplier;

/** The lines the column draws at the heights that matter for the world the editor shows: those of {@link Facts}. */
public final class FactMarkers implements Supplier<List<ColumnWidget.Marker>> {
    private final FactsNow now;

    private FactMarkers(FactsNow now) {
        this.now = now;
    }

    /** The lines for what the editor shows, which follow its biome and options. */
    public static FactMarkers of(EditorContext editor) {
        return new FactMarkers(new FactsNow(editor));
    }

    @Override
    public List<ColumnWidget.Marker> get() {
        return now.get().map(situation -> Facts.ALL.stream()
                .flatMap(fact -> fact.marker(situation).stream())
                .map(marker -> new ColumnWidget.Marker(marker.y(), marker.color()))
                .toList()).orElse(List.of());
    }
}
