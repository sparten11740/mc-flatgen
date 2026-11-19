package dev.flatgen.templates.client;

import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's way to the templates (F14): a button that opens them. */
public final class TemplatesPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        return List.of(new TextButton(Component.translatable("flatgen.templates.open"),
                flow.next(PanelSizes.WIDE_BUTTON), () -> editor.open(new TemplatesScreen(editor)), () -> true,
                () -> List.of(Component.translatable("flatgen.templates.open_tip"))));
    }
}
