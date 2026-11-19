package dev.flatgen.library.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.QuestionScreen;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.library.rules.MissingParts;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Using a saved preset in the editor, from the library or the editor's favourites, or exporting one: a preset naming
 * parts this game lacks asks first whether to go on with stand-ins. A used one is applied as a step to undo and the
 * editor shows again.
 */
final class PresetUse {
    private PresetUse() {
    }

    /** Uses a preset, asking from {@code asker} first when it names parts this game lacks. */
    static void use(EditorContext editor, Screen asker, Preset preset) {
        madeHere(editor, asker, preset, made -> applied(editor, made));
    }

    /**
     * Hands {@code then} the preset as this game can make it: as it is, or, when it names parts this game lacks, with
     * stand-ins once the player agrees, asked from {@code asker}; nothing when the player says no.
     */
    static void madeHere(EditorContext editor, Screen asker, Preset preset, Consumer<Preset> then) {
        MissingIds lacking = FlatSettings.missingIds(preset, editor.registries());
        if (lacking.isEmpty()) {
            then.accept(preset);
            return;
        }
        editor.open(new QuestionScreen(asker, Icon.MISSING, Component.translatable("flatgen.library.missing_title"),
                List.of(Component.translatable("flatgen.library.missing_question", listed(lacking))),
                () -> then.accept(MissingParts.replaced(preset, lacking))));
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
