package dev.flatgen.templates.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import net.minecraft.network.chat.Component;

/** The editor's way to the templates (F14): a button that opens them. */
public final class TemplatesPanel implements EditorPanel {
    /** Shown for every dimension, so the header keeps its place; usable while the overworld is edited. */
    @Override
    public boolean shows(EditorContext editor) {
        return true;
    }

    /** A button in the header. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.overworldTool(editor, Icon.item("minecraft:crafting_table"),
                Component.translatable("flatgen.templates.open"), () -> editor.open(new TemplatesScreen(editor)),
                Component.translatable("flatgen.templates.open_tip"));
    }
}
