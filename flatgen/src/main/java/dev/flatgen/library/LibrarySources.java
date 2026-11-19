package dev.flatgen.library;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;

/** Every source of the library's presets, in the order they are read; a new source is one more here. */
public final class LibrarySources {
    private LibrarySources() {
    }

    /** The sources for a world being created with these registries. */
    public static List<PresetSource> all(HolderLookup.Provider registries) {
        return List.of(PresetStore.player(), new GamePresets(registries), new WorldTypePresets(registries),
                new PackPresets());
    }

    /** Every preset of every source, read now. */
    public static List<PresetLibrary.Entry> entries(HolderLookup.Provider registries) {
        return all(registries).stream().flatMap(source -> source.presets().stream()).toList();
    }

    /** The preset the library names so, in any source; none when no source has it or it cannot be read. */
    public static Optional<Preset> find(String key, HolderLookup.Provider registries) {
        return entries(registries).stream().filter(entry -> entry.key().equals(key)).findFirst()
                .flatMap(PresetLibrary.Entry::preset);
    }
}
