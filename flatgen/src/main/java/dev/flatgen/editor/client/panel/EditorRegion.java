package dev.flatgen.editor.client.panel;

import dev.flatgen.client.gui.rules.PageFrame;

/**
 * The parts of the editor a panel places its widgets in, as the design canvas lays them out
 * (docs/design/simple-editor.md): each with the height of a row of controls in it, in GUI pixels.
 */
public enum EditorRegion {
    /** The header's tools, after the dimensions: Presets, Templates. */
    HEADER(20),
    /** The bar above the layers: adding a layer; in Advanced the ground's Y and the codes. */
    LAYERS_BAR(16),
    /** The middle: the layers, or what a dimension without its own layers shows. */
    LAYERS(20),
    /** The left: the world from the side, or what a dimension without its own layers shows there. */
    WORLD(20),
    /** The right: the world's settings, each panel on a raised box, scrolling when they do not fit. */
    ASIDE(18),
    /** The footer's left: undo, redo and what the last action said. */
    FOOTER(PageFrame.ACTION_HEIGHT);

    private final int rowHeight;

    EditorRegion(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    /** How tall a row of controls in it is, in GUI pixels. */
    public int rowHeight() {
        return rowHeight;
    }
}
