package dev.flatgen.preset.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Presets as the game's own superflat settings JSON: the fixtures are settings the game wrote into level.dat files. */
class VanillaSettingsTest {
    /** Water World, as a world created from the game's own preset holds it. */
    private static final String WATER_WORLD = """
            {"features": false, "biome": "minecraft:deep_ocean", "layers": [
              {"block": "minecraft:bedrock", "height": 1}, {"block": "minecraft:deepslate", "height": 64},
              {"block": "minecraft:stone", "height": 5}, {"block": "minecraft:dirt", "height": 5},
              {"block": "minecraft:gravel", "height": 5}, {"block": "minecraft:water", "height": 90}],
             "structure_overrides": ["minecraft:ocean_ruins", "minecraft:shipwrecks", "minecraft:ocean_monuments"],
             "lakes": false}""";

    @Test
    void readsAWorldsSettings() {
        PresetReading reading = VanillaSettings.fromJson(json(WATER_WORLD), "Water World", "minecraft:water_bucket");
        Preset preset = reading.preset();
        assertEquals(6, preset.layers().size());
        assertEquals(new Layer("minecraft:water", 90), preset.layers().get(5));
        assertEquals(170, preset.height());
        assertEquals("minecraft:deep_ocean", preset.biome());
        assertEquals(Optional.of(List.of("minecraft:ocean_ruins", "minecraft:shipwrecks", "minecraft:ocean_monuments")),
                preset.structureSets());
        assertEquals(false, preset.decorations());
        assertEquals("Water World", preset.name());
        assertEquals(Map.of(), preset.options());
        assertEquals(List.of(), reading.problems());
    }

    @Test
    void writesWhatItReads() {
        JsonObject settings = json(WATER_WORLD);
        Preset preset = VanillaSettings.fromJson(settings, "", "").preset();
        assertEquals(settings, VanillaSettings.toJson(preset));
    }

    @Test
    void writesTheDecorationsAsFeatures() {
        JsonObject settings = VanillaSettings.toJson(TestPresets.TALL_FLAT);
        assertEquals(false, settings.get("features").getAsBoolean());
        assertEquals(4, settings.getAsJsonArray("layers").size());
        assertEquals("minecraft:plains", settings.get("biome").getAsString());
    }

    @Test
    void leavesOutTheStructureListWhenTheBiomeDecides() {
        Preset preset = VanillaSettings.fromJson(json("{\"layers\": []}"), "", "").preset();
        assertEquals(Optional.empty(), preset.structureSets());
        assertEquals(false, VanillaSettings.toJson(preset).has("structure_overrides"));
    }

    @Test
    void readsAndWritesATagOfStructureSetsAsTheGameDoes() {
        JsonObject settings = json("{\"layers\": [], \"structure_overrides\": \"#minecraft:villages\"}");
        Preset preset = VanillaSettings.fromJson(settings, "", "").preset();
        assertEquals(Optional.of(List.of("#minecraft:villages")), preset.structureSets());
        assertEquals("#minecraft:villages", VanillaSettings.toJson(preset).get("structure_overrides").getAsString());
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }
}
