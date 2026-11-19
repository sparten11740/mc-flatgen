package dev.flatgen.library;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.mc.FlatWorldTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;

/**
 * The game's and its data packs' world types that make more than the overworld flat (F25), the game's Flat All
 * Dimensions from 26.2 on, each a preset with its Nether and End, from the registries of the world being created.
 *
 * @param registries those registries
 */
public record WorldTypePresets(HolderLookup.Provider registries) implements PresetSource {
    /** How a world type's key begins in the library, before its id. */
    private static final String KEY_PREFIX = "world_type:";

    @Override
    public List<PresetLibrary.Entry> presets() {
        return FlatWorldTypes.presets(registries).entrySet().stream()
                .map(preset -> new PresetLibrary.Entry(KEY_PREFIX + preset.getKey(), GamePresets.KIND,
                        Optional.of(preset.getValue().preset()), preset.getValue().problems()))
                .toList();
    }
}
