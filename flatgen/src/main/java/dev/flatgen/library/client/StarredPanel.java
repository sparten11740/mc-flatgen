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
 * The editor's starred presets (F12): a row with a picture of each, its top block, which uses it in one click; the
 * library is where they are starred.
 */
public final class StarredPanel implements EditorPanel {
    /** The starred presets as last found, kept until the player's presets or choices change, and the change after. */
    private List<StarredPreset> found = List.of();
    private int foundAt = -1;

    /**
     * A starred preset found among the library's sources.
     *
     * @param key    how the library names it
     * @param preset the preset
     */
    private record StarredPreset(String key, Preset preset) {
    }

    /** The starred presets, a button each, on a box of the aside. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (!starred(editor).isEmpty()) {
            layout.box(inside -> content(editor, inside));
        }
    }

    private List<Widget> content(EditorContext editor, FlowLayout flow) {
        List<Widget> widgets = new ArrayList<>();
        for (StarredPreset starred : starred(editor)) {
            Preset preset = starred.preset();
            widgets.add(new PictureButton(Icon.item(preset.icon()), flow.next(PictureButton.SIZE),
                    () -> use(editor, starred.key(), preset),
                    () -> List.of(Component.literal(preset.name()),
                            Component.translatable("flatgen.library.use_tip"))));
        }
        return widgets;
    }

    /** The player's starred presets, found again among the library's sources only after the library changed. */
    private List<StarredPreset> starred(EditorContext editor) {
        if (foundAt != LibraryChanges.count()) {
            foundAt = LibraryChanges.count();
            List<String> keys = ChoicesFile.read().picks().starred();
            List<PresetLibrary.Entry> entries = keys.isEmpty() ? List.of()
                    : LibrarySources.entries(editor.registries());
            found = keys.stream().flatMap(key -> entries.stream().filter(entry -> entry.key().equals(key))
                    .findFirst().flatMap(PresetLibrary.Entry::preset).map(preset -> new StarredPreset(key, preset))
                    .stream()).toList();
        }
        return found;
    }

    private static void use(EditorContext editor, String key, Preset preset) {
        ChoicesFile.save(ChoicesFile.read().used(key));
        PresetUse.use(editor, editor.screen(), preset);
    }
}
