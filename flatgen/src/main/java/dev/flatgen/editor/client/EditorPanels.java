package dev.flatgen.editor.client;

import dev.flatgen.biomes.client.BiomePanel;
import dev.flatgen.dimensions.client.DimensionPanel;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.facts.client.FactsPanel;
import dev.flatgen.facts.client.NoSlimesPanel;
import dev.flatgen.layers.client.LayerPanel;
import dev.flatgen.library.client.FavouritesPanel;
import dev.flatgen.library.client.LibraryPanel;
import dev.flatgen.options.client.OptionsPanel;
import dev.flatgen.structures.client.StructuresPanel;
import dev.flatgen.templates.client.TemplatesPanel;
import java.util.List;

/**
 * The editor's panels, in the order they place their widgets in each region; a feature that adds controls to the
 * editor adds its panel here.
 */
final class EditorPanels {
    /** Every panel, in the order they are laid out. */
    static final List<EditorPanel> ALL = List.of(
            new DimensionPanel(),
            new SummaryPanel(),
            new BiomePanel(),
            new FactsPanel(),
            new NoSlimesPanel(),
            new OptionsPanel(),
            new FavouritesPanel(),
            new StructuresPanel(),
            new LibraryPanel(),
            new TemplatesPanel(),
            new CodePanel(),
            new LayerPanel(),
            new HistoryPanel(),
            new MessagePanel(),
            new GameEditorPanel());

    private EditorPanels() {
    }
}
