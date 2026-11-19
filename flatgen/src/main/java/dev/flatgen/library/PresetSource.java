package dev.flatgen.library;

import dev.flatgen.library.rules.PresetLibrary;
import java.util.List;

/**
 * Where presets of the library come from (plan 3.4): the player's folder, the game's flat world presets and flat world
 * types, or preset files of mods and resource packs. The library lists every source without knowing its kind; a new
 * source is a new class in {@link LibrarySources}.
 */
public interface PresetSource {
    /** Every preset of this source, read now. */
    List<PresetLibrary.Entry> presets();
}
