package dev.flatgen.library;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.mc.FlatPresets;
import dev.flatgen.preset.rules.PresetReading;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;

/**
 * The game's and its data packs' flat world presets (F13), Classic Flat, Tunnelers' Dream and the rest, from the
 * registries of the world being created.
 *
 * @param registries those registries
 */
public record GamePresets(HolderLookup.Provider registries) implements PresetSource {
    /** How a game preset's key begins in the library, before its id. */
    private static final String KEY_PREFIX = "game:";
    /** Built in, after the player's own, hidden by any setting that hides the game's. */
    private static final PresetLibrary.Kind KIND = new PresetLibrary.Kind(1, false,
            Optional.of(PresetLibrary.BuiltIns.NO_GAME));

    @Override
    public List<PresetLibrary.Entry> presets() {
        return entries(KEY_PREFIX, FlatPresets.visible(registries));
    }

    /** The game's presets read from it, by id, as the library's entries, each key the prefix before its id. */
    static List<PresetLibrary.Entry> entries(String keyPrefix, Map<String, PresetReading> presets) {
        return presets.entrySet().stream()
                .map(preset -> new PresetLibrary.Entry(keyPrefix + preset.getKey(), KIND,
                        Optional.of(preset.getValue().preset()), preset.getValue().problems()))
                .toList();
    }
}
