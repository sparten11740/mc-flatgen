package dev.flatgen.editor.client;

import dev.flatgen.biomes.client.BiomePanel;
import dev.flatgen.blocks.client.BlockPanel;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.facts.client.FactsPanel;
import dev.flatgen.layers.client.LayerPanel;
import dev.flatgen.library.client.FavouritesPanel;
import dev.flatgen.library.client.LibraryPanel;
import dev.flatgen.options.client.OptionsPanel;
import dev.flatgen.structures.client.StructuresPanel;
import java.util.List;

/** The panels beside the editor's column, top down; a feature that adds controls to the editor adds its panel here. */
final class EditorPanels {
    /** Every panel, in the order they are laid out. */
    static final List<EditorPanel> ALL = List.of(
            new SummaryPanel(),
            new FavouritesPanel(),
            new FactsPanel(),
            new BiomePanel(),
            new StructuresPanel(),
            new LibraryPanel(),
            new BlockPanel(),
            new LayerPanel(),
            new HistoryPanel(),
            new CodePanel(),
            new MessagePanel(),
            new OptionsPanel());

    private EditorPanels() {
    }
}
