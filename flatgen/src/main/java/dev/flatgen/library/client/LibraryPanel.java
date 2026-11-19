package dev.flatgen.library.client;

import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's way to the preset library (F11): a button that opens it. */
public final class LibraryPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        return List.of(new TextButton(Component.translatable("flatgen.library.open"),
                flow.next(PanelSizes.WIDE_BUTTON), () -> editor.open(new LibraryScreen(editor)), () -> true,
                () -> List.of(Component.translatable("flatgen.library.open_tip"))));
    }
}
