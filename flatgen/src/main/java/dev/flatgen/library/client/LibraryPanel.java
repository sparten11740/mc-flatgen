package dev.flatgen.library.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.library.PresetImports;
import dev.flatgen.library.PresetStore;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's way to the preset library (F11): a button that opens it; preset files dropped on the editor join it. */
public final class LibraryPanel implements EditorPanel {
    /** A button in the header. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.button(EditorRegion.HEADER, Icon.item("minecraft:bookshelf"), Component.translatable("flatgen.library.open"),
                () -> editor.open(new LibraryScreen(editor)), () -> true,
                () -> List.of(Component.translatable("flatgen.library.open_tip")));
    }

    /** Adds dropped preset files to the player's presets, the editor saying what was added and left out. */
    @Override
    public boolean filesDropped(EditorContext editor, List<Path> files) {
        editor.say(Imports.summary(PresetImports.fromFiles(files, PresetStore.player(), Imports.unnamed())));
        return true;
    }
}
