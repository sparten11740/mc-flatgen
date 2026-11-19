package dev.flatgen.library.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.library.ChoicesFile;
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
    /** The favourites as last found, and the keys they were found for, kept until the player's favourites change. */
    private List<String> foundFor = List.of();
    private List<Favourite> found = List.of();

    /**
     * A favourite preset found among the library's sources.
     *
     * @param key    how the library names it
     * @param preset the preset
     */
    private record Favourite(String key, Preset preset) {
    }

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        List<Widget> widgets = new ArrayList<>();
        flow.newRow();
        for (Favourite favourite : favourites(editor)) {
            Preset preset = favourite.preset();
            widgets.add(new PictureButton(Icon.item(preset.icon()), flow.next(PictureButton.SIZE),
                    () -> use(editor, favourite.key(), preset),
                    () -> List.of(Component.literal(preset.name()),
                            Component.translatable("flatgen.library.use_tip"))));
        }
        return widgets;
    }

    /** The player's favourites, found again among the library's sources only when they changed. */
    private List<Favourite> favourites(EditorContext editor) {
        List<String> starred = ChoicesFile.read().picks().starred();
        if (!starred.equals(foundFor)) {
            foundFor = starred;
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
