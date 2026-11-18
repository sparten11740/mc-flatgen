package dev.flatgen.editor.client;

import dev.flatgen.config.SettingsFile;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * The world types whose Customize button opens Flatgen, and the editor it opens. Each loader's platform half hands
 * these to the game its own way, so this is the one place that says what Flatgen edits.
 */
public final class PresetEditors {
    /** The world types Flatgen edits. */
    public static final List<ResourceKey<WorldPreset>> EDITED = List.of(WorldPresets.FLAT);

    /** The game's own superflat editor, which Flatgen's screen offers and which returns when Flatgen is turned off. */
    @SuppressWarnings("deprecation")
    static final PresetEditor VANILLA = PresetEditor.EDITORS.get(Optional.of(WorldPresets.FLAT));

    /** Flatgen's editor: its screen, or the game's own when the player has turned Flatgen off. */
    public static final PresetEditor EDITOR = (parent, context) -> SettingsFile.read().enabled()
            ? new EditorScreen(parent, context)
            : VANILLA.createEditScreen(parent, context);

    private PresetEditors() {
    }

    /** Whether Flatgen edits this world type. */
    public static boolean edits(Holder<WorldPreset> preset) {
        return EDITED.stream().anyMatch(preset::is);
    }
}
