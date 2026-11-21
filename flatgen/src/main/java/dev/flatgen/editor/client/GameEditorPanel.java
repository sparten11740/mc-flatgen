package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import net.minecraft.network.chat.Component;

/** In Advanced, a button at the aside's end that opens the game's own Superflat editor on the layers as they are now. */
final class GameEditorPanel implements EditorPanel {
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (!editor.advanced()) {
            return;
        }
        Rect place = layout.flow(EditorRegion.ASIDE).line(EditorRegion.ASIDE.rowHeight());
        layout.add(EditorRegion.ASIDE, new TextButton(Icon.line("external"),
                Component.translatable("flatgen.editor.vanilla"), place, editor::openGameEditor));
    }
}
