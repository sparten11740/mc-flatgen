package dev.flatgen.library.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.library.PresetImports;
import dev.flatgen.library.PresetStore;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's way to the preset library (F11): a button that opens it; preset files dropped on the editor join it. */
public final class LibraryPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        return List.of(new PictureButton(Icon.item("minecraft:bookshelf"), flow.next(PictureButton.SIZE),
                () -> editor.open(new LibraryScreen(editor)), () -> List.of(
                        Component.translatable("flatgen.library.open"),
                        Component.translatable("flatgen.library.open_tip"))));
    }

    /** Adds dropped preset files to the player's presets, the editor saying what was added and left out. */
    @Override
    public boolean filesDropped(EditorContext editor, List<Path> files) {
        editor.say(ImportSummary.of(PresetImports.fromFiles(files, PresetStore.player())));
        return true;
    }
}
