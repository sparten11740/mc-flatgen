package dev.flatgen.editor.client;

import dev.flatgen.client.gui.TextLines;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.rules.EditorState;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's summary line: where the surface is. */
final class SummaryPanel implements EditorPanel {
    /** Shown for every dimension the preset gives a stack of its own (F22). */
    @Override
    public boolean shows(EditorState state) {
        return state.stack().isPresent();
    }

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        return List.of(new TextLines(flow.line(editor.lineHeight()), () -> List.of(Component.translatable(
                "flatgen.editor.surface", editor.world().floor() + editor.shown().height()))));
    }
}
