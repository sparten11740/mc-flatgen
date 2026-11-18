package dev.flatgen.preset.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.flatgen.json.rules.JsonFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Presets as the game's own superflat settings are written in JSON, in a world's level.dat and by the settings' codec:
 * {@code layers}, {@code biome}, {@code lakes}, {@code features} (the decorations) and {@code structure_overrides}.
 * Going through this JSON lets Flatgen use the game's codec rather than its settings class, whose decoration and lake
 * switches have no getters. A world's settings carry no name, icon or options.
 */
public final class VanillaSettings {
    private static final String LAYERS = "layers";
    private static final String BIOME = "biome";
    private static final String LAKES = "lakes";
    private static final String FEATURES = "features";
    private static final String STRUCTURE_OVERRIDES = "structure_overrides";
    /** How the codec writes a tag of structure sets in place of a list. */
    private static final String TAG_MARK = "#";

    private VanillaSettings() {
    }

    /** The game's settings JSON for a preset. */
    public static JsonObject toJson(Preset preset) {
        JsonObject settings = new JsonObject();
        settings.add(LAYERS, LayerJson.write(preset.layers()));
        settings.addProperty(BIOME, preset.biome());
        settings.addProperty(LAKES, preset.lakes());
        settings.addProperty(FEATURES, preset.decorations());
        preset.structureSets().ifPresent(sets -> settings.add(STRUCTURE_OVERRIDES, structureOverrides(sets)));
        return settings;
    }

    /** A preset of this name and icon from the game's settings JSON. */
    public static PresetReading fromJson(JsonObject settings, String name, String icon) {
        List<String> problems = new ArrayList<>();
        List<Layer> layers = JsonFields.array(settings, LAYERS, problems)
                .map(array -> LayerJson.read(array, problems)).orElse(List.of());
        Preset preset = new Preset(name, icon, layers,
                JsonFields.text(settings, BIOME, Preset.DEFAULT_BIOME, problems),
                structureSets(settings, problems),
                JsonFields.bool(settings, FEATURES, false, problems),
                JsonFields.bool(settings, LAKES, false, problems),
                Map.of());
        return new PresetReading(preset, problems);
    }

    private static JsonElement structureOverrides(List<String> sets) {
        boolean isOneTag = sets.size() == 1 && sets.get(0).startsWith(TAG_MARK);
        return isOneTag ? new JsonPrimitive(sets.get(0)) : JsonFields.textArray(sets);
    }

    private static Optional<List<String>> structureSets(JsonObject settings, List<String> problems) {
        JsonElement overrides = settings.get(STRUCTURE_OVERRIDES);
        if (overrides != null && JsonFields.isText(overrides)) {
            return Optional.of(List.of(overrides.getAsString()));
        }
        return JsonFields.array(settings, STRUCTURE_OVERRIDES, problems)
                .map(array -> JsonFields.texts(array, STRUCTURE_OVERRIDES, problems));
    }
}
