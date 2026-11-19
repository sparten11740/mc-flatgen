package dev.flatgen.library;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.mc.Resources;
import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.preset.rules.PresetReading;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * The built-in presets of mods and resource packs (F13), Flatgen's starters among them: files
 * {@code assets/<namespace>/flatgen/presets/<name>.json} in F02's format, their names translation keys or the names
 * themselves.
 */
public final class PackPresets implements PresetSource {
    /** The folder of {@code assets/<namespace>/} preset files are in. */
    private static final String FOLDER = "flatgen/presets";
    /** How a pack preset's key begins in the library, before its id. */
    private static final String KEY_PREFIX = "pack:";

    @Override
    public List<PresetLibrary.Entry> presets() {
        return Resources.jsonFiles(FOLDER).entrySet().stream().map(PackPresets::entry).toList();
    }

    private static PresetLibrary.Entry entry(Map.Entry<String, String> file) {
        String key = KEY_PREFIX + file.getKey();
        try {
            PresetReading reading = PresetJson.read(file.getValue());
            String name = Component.translatable(reading.preset().name()).getString();
            return new PresetLibrary.Entry(key, PresetLibrary.Origin.PACK,
                    Optional.of(reading.preset().withName(name)), reading.problems());
        } catch (IllegalArgumentException exception) {
            return new PresetLibrary.Entry(key, PresetLibrary.Origin.PACK, Optional.empty(),
                    List.of(String.valueOf(exception.getMessage())));
        }
    }
}
