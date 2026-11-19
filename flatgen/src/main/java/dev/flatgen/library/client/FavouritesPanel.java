package dev.flatgen.library.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.library.ChoicesFile;
import dev.flatgen.library.LibraryChanges;
import dev.flatgen.library.LibrarySources;
import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * The editor's favourite presets (F12): a row with a picture of each, its top block, which uses it in one click; the
 * library is where they are starred.
 */
public final class FavouritesPanel implements EditorPanel {
    /** The favourites as last found, kept until the player's presets or choices change, and the change they follow. */
    private List<Favourite> found = List.of();
    private int foundAt = -1;

    /**
     * A favourite preset found among the library's sources.
     *
     * @param key    how the library names it
     * @param preset the preset
     */
    private record Favourite(String key, Preset preset) {
    }

    /** The favourite presets, a button each, on a box of the aside. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (!favourites(editor).isEmpty()) {
            layout.box(inside -> content(editor, inside));
        }
    }

    private List<Widget> content(EditorContext editor, FlowLayout flow) {
        List<Widget> widgets = new ArrayList<>();
        for (Favourite favourite : favourites(editor)) {
            Preset preset = favourite.preset();
            widgets.add(new PictureButton(Icon.item(preset.icon()), flow.next(PictureButton.SIZE),
                    () -> use(editor, favourite.key(), preset),
                    () -> List.of(Component.literal(preset.name()),
                            Component.translatable("flatgen.library.use_tip"))));
        }
        return widgets;
    }

    /** The player's favourites, found again among the library's sources only after the library changed. */
    private List<Favourite> favourites(EditorContext editor) {
        if (foundAt != LibraryChanges.count()) {
            foundAt = LibraryChanges.count();
            List<String> starred = ChoicesFile.read().picks().starred();
            List<PresetLibrary.Entry> entries = starred.isEmpty() ? List.of()
                    : LibrarySources.entries(editor.registries());
            found = starred.stream().flatMap(key -> entries.stream().filter(entry -> entry.key().equals(key))
                    .findFirst().flatMap(PresetLibrary.Entry::preset).map(preset -> new Favourite(key, preset))
                    .stream()).toList();
        }
        return found;
    }

    private static void use(EditorContext editor, String key, Preset preset) {
        ChoicesFile.save(ChoicesFile.read().used(key));
        PresetUse.use(editor, editor.screen(), preset);
    }
}
