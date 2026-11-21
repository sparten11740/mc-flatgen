package dev.flatgen.facts.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.rules.Fact;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.facts.rules.Situation;
import dev.flatgen.layers.client.ColumnWidget;
import java.util.List;
import java.util.Optional;
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
                .flatMap(fact -> marker(fact, situation).stream())
                .toList()).orElse(List.of());
    }

    /** A fact's line for this world, if it draws one, ending in its picture. */
    private static Optional<ColumnWidget.Marker> marker(Fact fact, Situation situation) {
        return fact.marker(situation).map(line -> new ColumnWidget.Marker(line.y(), line.color(),
                Icon.of(line.icon())));
    }
}
