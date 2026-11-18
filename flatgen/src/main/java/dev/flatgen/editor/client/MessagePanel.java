package dev.flatgen.editor.client;

import dev.flatgen.client.gui.TextLines;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's message line: what the last copy or paste said, until the next edit. */
final class MessagePanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        return List.of(new TextLines(flow.line(editor.lineHeight()),
                () -> List.of(editor.message() == null ? Component.empty() : editor.message())));
    }
}
