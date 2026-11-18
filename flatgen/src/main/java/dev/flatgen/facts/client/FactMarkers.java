package dev.flatgen.facts.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.rules.WorldFacts;
import dev.flatgen.layers.client.ColumnWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The lines the column draws at the heights that matter for the biome shown: where slimes stop spawning in slime
 * chunks, when slimes spawn in it, and where its rain turns to snow, when it does.
 */
public final class FactMarkers implements Supplier<List<ColumnWidget.Marker>> {
    private static final int SLIME_GREEN = 0xFF7FD34F;
    private static final int SNOW_BLUE = 0xFF8FD8FF;

    private final FactsNow facts;

    private FactMarkers(FactsNow facts) {
        this.facts = facts;
    }

    /** The lines for what the editor shows, which follow its biome and options. */
    public static FactMarkers of(EditorContext editor) {
        return new FactMarkers(new FactsNow(editor));
    }

    @Override
    public List<ColumnWidget.Marker> get() {
        Optional<BiomeInfo> biome = facts.biome();
        if (biome.isEmpty()) {
            return List.of();
        }
        List<ColumnWidget.Marker> markers = new ArrayList<>();
        if (biome.get().spawns().slimes() && facts.mobSpawning()) {
            markers.add(new ColumnWidget.Marker(WorldFacts.SLIME_CHUNK_MAX_Y, SLIME_GREEN));
        }
        WorldFacts.snowLine(biome.get().climate()).ifPresent(y -> markers.add(new ColumnWidget.Marker(y, SNOW_BLUE)));
        return markers;
    }
}
