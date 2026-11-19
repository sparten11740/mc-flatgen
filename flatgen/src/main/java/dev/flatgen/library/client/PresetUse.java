package dev.flatgen.library.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.QuestionScreen;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.library.rules.MissingParts;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Using a saved preset in the editor, from the library or the editor's favourites: it is applied as a step to undo
 * and the editor shows again; one naming parts this game lacks asks first whether to use it with stand-ins.
 */
final class PresetUse {
    private PresetUse() {
    }

    /** Uses a preset, asking from {@code asker} first when it names parts this game lacks. */
    static void use(EditorContext editor, Screen asker, Preset preset) {
        MissingIds lacking = FlatSettings.missingIds(preset, editor.registries());
        if (lacking.isEmpty()) {
            applied(editor, preset);
            return;
        }
        editor.open(new QuestionScreen(asker, Icon.MISSING, Component.translatable("flatgen.library.missing_title"),
                List.of(Component.translatable("flatgen.library.missing_question", listed(lacking))),
                () -> applied(editor, MissingParts.replaced(preset, lacking))));
    }

    /** The ids a preset names that this game lacks, in a line. */
    static String listed(MissingIds lacking) {
        return String.join(", ", Stream.of(lacking.blocks(), lacking.biomes(), lacking.structureSets())
                .flatMap(List::stream).toList());
    }

    private static void applied(EditorContext editor, Preset preset) {
        editor.use(preset);
        editor.open(editor.screen());
    }
}
