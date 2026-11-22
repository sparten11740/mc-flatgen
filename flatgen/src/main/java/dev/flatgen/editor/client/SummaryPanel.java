package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Chip;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.layers.client.LayerText;

/** How much air is above the stack, on a chip over the world's picture, whose column marks the world's top. */
final class SummaryPanel implements EditorPanel {

    /** Shown for every dimension the preset gives a stack of its own (F22). */
    @Override
    public boolean shows(EditorContext editor) {
        return editor.shown().stack().isPresent();
    }

    /** How much air is above the ground, on a chip at the top of the world. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.add(EditorRegion.WORLD, Chip.inCorner(Icon.line("arrow_up"),
                () -> LayerText.air(editor.world(), editor.shown().height()), layout.area(EditorRegion.WORLD)));
    }
}
