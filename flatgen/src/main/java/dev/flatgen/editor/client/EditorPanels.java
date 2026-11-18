package dev.flatgen.editor.client;

import dev.flatgen.blocks.client.BlockPanel;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.layers.client.LayerPanel;
import java.util.List;

/** The panels beside the editor's column, top down; a feature that adds controls to the editor adds its panel here. */
final class EditorPanels {
    /** Every panel, in the order they are laid out. */
    static final List<EditorPanel> ALL = List.of(
            new SummaryPanel(),
            new BlockPanel(),
            new LayerPanel(),
            new HistoryPanel(),
            new CodePanel(),
            new MessagePanel());

    private EditorPanels() {
    }
}
