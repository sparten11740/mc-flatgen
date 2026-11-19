package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Chip;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.editor.rules.EditorState;
import net.minecraft.network.chat.Component;

/** How much air is above the stack, up to the world's top, on a chip over the world's picture. */
final class SummaryPanel implements EditorPanel {

    /** Shown for every dimension the preset gives a stack of its own (F22). */
    @Override
    public boolean shows(EditorState state) {
        return state.stack().isPresent();
    }

    /** How much air is above the ground, up to the world's top, on a chip at the top of the world. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.add(EditorRegion.WORLD, Chip.inCorner(Icon.line("arrow_up"), () -> air(editor),
                layout.area(EditorRegion.WORLD)));
    }

    /** The blocks of air above the ground, and the Y of the world's top. */
    private static Component air(EditorContext editor) {
        int top = editor.world().floor() + editor.world().height() - 1;
        return Component.translatable("flatgen.editor.air", editor.world().height() - editor.shown().height(), top);
    }
}
