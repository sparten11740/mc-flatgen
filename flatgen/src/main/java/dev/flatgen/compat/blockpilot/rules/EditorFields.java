package dev.flatgen.compat.blockpilot.rules;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.LinkedHashMap;
import java.util.Map;

/** What {@code flatgen:editor} reports of the preset in Flatgen's editor, by field name, in the order it says them. */
public final class EditorFields {
    private EditorFields() {
    }

    /**
     * The fields of an open editor in this state, its edited stack in a world whose floor is at this Y; {@code code} is
     * the overworld's superflat code, {@code selected} is {@code l<n>}, bottom first, or {@code none}.
     */
    public static Map<String, Object> of(EditorState state, int worldBottom) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("open", true);
        fields.put("code", VanillaCode.write(state.preset()));
        fields.put("dimension", state.edited().key());
        fields.put("flat", state.stack().isPresent());
        fields.put("layers", state.layers().size());
        fields.put("height", state.height());
        fields.put("surface_y", worldBottom + state.height());
        fields.put("biome", state.biome());
        fields.put("selected", state.selected() >= 0 ? "l" + state.selected() : "none");
        return fields;
    }
}
