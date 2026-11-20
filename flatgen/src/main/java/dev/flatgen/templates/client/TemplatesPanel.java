package dev.flatgen.templates.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's way to the templates (F14): a button that opens them. */
public final class TemplatesPanel implements EditorPanel {
    /** A button in the header. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.button(EditorRegion.HEADER, Icon.item("minecraft:crafting_table"), Component.translatable("flatgen.templates.open"),
                () -> editor.open(new TemplatesScreen(editor)), () -> true,
                () -> List.of(Component.translatable("flatgen.templates.open_tip")));
    }
}
