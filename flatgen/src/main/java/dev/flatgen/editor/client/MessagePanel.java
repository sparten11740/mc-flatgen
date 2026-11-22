package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import net.minecraft.network.chat.Component;

/** The editor's message line: what the last copy or paste said, until the next edit. */
final class MessagePanel implements EditorPanel {
    /** Shown for every dimension, with a stack or without (F22). */
    @Override
    public boolean shows(EditorContext editor) {
        return true;
    }

    /** What the last copy or paste said, after undo and redo in the footer. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.add(EditorRegion.FOOTER, new Caption(() -> editor.message() == null ? Component.empty()
                : editor.message(), layout.flow(EditorRegion.FOOTER).rest(), TextStyle.BODY, Look.TEXT));
    }
}
