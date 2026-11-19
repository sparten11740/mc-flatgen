package dev.flatgen.editor.client;

import dev.flatgen.config.SettingsFile;
import dev.flatgen.mc.WorldTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * The world types whose Customize button opens Flatgen, and the editor it opens: Superflat, and a mod's world type
 * that is superflat underneath and has no editor of its own (F18). This is the one place that says what Flatgen edits.
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

    /** Whether Flatgen edits this world type in place of the game's own editor. */
    public static boolean edits(Holder<WorldPreset> preset) {
        return EDITED.stream().anyMatch(preset::is);
    }

    /**
     * The editor a world type's Customize button opens: Flatgen's for Superflat, and for a type superflat underneath
     * with no editor of its own; else the one the game found, none when it found none.
     */
    public static Optional<PresetEditor> editorFor(Holder<WorldPreset> type, Optional<PresetEditor> found,
            HolderLookup.Provider registries) {
        boolean flatUnderneath = found.isEmpty() && WorldTypes.onlyFlatOverworld(type, registries);
        return edits(type) || flatUnderneath ? Optional.of(EDITOR) : found;
    }

    /**
     * Whether Flatgen's editor can have set this world type's settings, world options included: Superflat, or a type
     * superflat underneath.
     */
    public static boolean mayHaveEdited(Holder<WorldPreset> type, HolderLookup.Provider registries) {
        return edits(type) || WorldTypes.onlyFlatOverworld(type, registries);
    }
}
