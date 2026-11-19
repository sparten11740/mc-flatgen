package dev.flatgen.library;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.mc.FlatPresets;
import java.util.List;
import net.minecraft.core.HolderLookup;

/**
 * The game's and its data packs' flat world presets (F13), Classic Flat, Tunnelers' Dream and the rest, from the
 * registries of the world being created.
 *
 * @param registries those registries
 */
public record GamePresets(HolderLookup.Provider registries) implements PresetSource {
    @Override
    public List<PresetLibrary.Entry> presets() {
        return FlatPresets.visible(registries);
    }
}
