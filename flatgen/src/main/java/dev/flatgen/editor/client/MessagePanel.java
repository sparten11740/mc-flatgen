package dev.flatgen.editor.client;

import dev.flatgen.client.gui.TextLines;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.editor.rules.EditorState;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's message line: what the last copy or paste said, until the next edit. */
final class MessagePanel implements EditorPanel {
    /** Shown for every dimension, with a stack or without (F22). */
    @Override
    public boolean shows(EditorState state) {
        return true;
    }

    /** What the last copy or paste said, after undo and redo in the footer. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.add(EditorRegion.FOOTER, new TextLines(layout.flow(EditorRegion.FOOTER).rest(),
                () -> List.of(editor.message() == null ? Component.empty() : editor.message())));
    }
}
