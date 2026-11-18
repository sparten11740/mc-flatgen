package dev.flatgen.compat.blockpilot.rules;

import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.LinkedHashMap;
import java.util.Map;

/** What {@code flatgen:editor} reports of the preset in Flatgen's editor, by field name, in the order it says them. */
public final class EditorFields {
    private EditorFields() {
    }

    /** The fields of an open editor holding this preset, in a world whose floor is at this Y. */
    public static Map<String, Object> of(Preset preset, int worldBottom) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("open", true);
        fields.put("code", VanillaCode.write(preset));
        fields.put("layers", preset.layers().size());
        fields.put("height", preset.height());
        fields.put("surface_y", worldBottom + preset.height());
        fields.put("biome", preset.biome());
        return fields;
    }
}
