package dev.flatgen.preset.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Flatgen's preset files: what one release writes, every later one reads, and a broken part never breaks the rest. */
class PresetJsonTest {
    @Test
    void readsWhatItWrites() {
        PresetReading reading = PresetJson.read(PresetJson.write(TestPresets.TALL_FLAT));
        assertEquals(TestPresets.TALL_FLAT, reading.preset());
        assertEquals(List.of(), reading.problems());
    }

    @Test
    void readsTheFormatTheBacklogDescribes() {
        String file = """
                {
                  "format": 1,
                  "name": "Tall flat",
                  "icon": "minecraft:grass_block",
                  "layers": [
                    { "block": "minecraft:bedrock", "height": 1 },
                    { "block": "minecraft:stone", "height": 100 },
                    { "block": "minecraft:dirt", "height": 2 },
                    { "block": "minecraft:grass_block", "height": 1 }
                  ],
                  "biome": "minecraft:plains",
                  "structures": ["minecraft:strongholds", "minecraft:villages"],
                  "decorations": false,
                  "lakes": false,
                  "options": { "flatgen:always_day": true }
                }
                """;
        assertEquals(TestPresets.TALL_FLAT, PresetJson.read(file).preset());
    }

    @Test
    void readsWhatItWritesOfTheNetherAndTheEnd() {
        Preset both = TestPresets.TALL_FLAT
                .withStack(Dimension.NETHER, Optional.of(new Stack(List.of(new Layer("minecraft:bedrock", 1),
                        new Layer("minecraft:netherrack", 4)), "minecraft:crimson_forest")))
                .withStack(Dimension.END, Optional.of(new Stack(List.of(new Layer("minecraft:end_stone", 2)),
                        "minecraft:the_end")));
        PresetReading reading = PresetJson.read(PresetJson.write(both));
        assertEquals(both, reading.preset());
        assertEquals(List.of(), reading.problems());
    }

    @Test
    void readsAnEndAloneWithItsOwnBiomeAndNotesANetherWithoutLayers() {
        PresetReading reading = PresetJson.read("{\"layers\": [{\"block\": \"minecraft:sand\", \"height\": 3}],"
                + " \"end\": {\"layers\": [{\"block\": \"minecraft:end_stone\", \"height\": 1}]},"
                + " \"nether\": {\"biome\": \"minecraft:basalt_deltas\"}}");
        assertEquals(Optional.of(new Stack(List.of(), "minecraft:basalt_deltas")),
                reading.preset().stack(Dimension.NETHER));
        assertEquals(List.of("no layers, so the nether is empty"), reading.problems());
        assertEquals(Optional.of(new Stack(List.of(new Layer("minecraft:end_stone", 1)), "minecraft:the_end")),
                reading.preset().stack(Dimension.END));
    }

    @Test
    void givesMissingKeysTheirDefaults() {
        PresetReading reading = PresetJson.read("{\"layers\": [{\"block\": \"minecraft:sand\", \"height\": 3}]}");
        Preset preset = reading.preset();
        assertEquals("", preset.name());
        assertEquals("minecraft:sand", preset.icon());
        assertEquals(Preset.DEFAULT_BIOME, preset.biome());
        assertEquals(Optional.empty(), preset.structureSets());
        assertEquals(Map.of(), preset.options());
        assertEquals(List.of(), reading.problems());
    }

    @Test
    void notesAMissingLayerListAndReadsTheRest() {
        PresetReading reading = PresetJson.read("{\"name\": \"Void\", \"biome\": \"minecraft:the_void\"}");
        assertEquals(List.of(), reading.preset().layers());
        assertEquals("minecraft:the_void", reading.preset().biome());
        assertEquals(PresetJson.DEFAULT_ICON, reading.preset().icon());
        assertEquals(List.of("no layers, so the world is empty"), reading.problems());
    }

    @Test
    void leavesOutLayersItCannotReadAndSaysWhich() {
        PresetReading reading = PresetJson.read("""
                {"layers": [
                  {"block": "minecraft:bedrock", "height": 1},
                  {"block": "minecraft:stone", "height": 0},
                  {"height": 4},
                  "minecraft:dirt",
                  {"block": "minecraft:grass_block", "height": 1}
                ]}""");
        assertEquals(List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:grass_block", 1)),
                reading.preset().layers());
        assertEquals(3, reading.problems().size());
        assertTrue(reading.problems().get(0).startsWith("layer 2 "));
    }

    @Test
    void keepsDefaultsForValuesOfTheWrongKindAndNotesThem() {
        PresetReading reading = PresetJson.read(
                "{\"layers\": [], \"lakes\": \"yes\", \"biome\": 7, \"options\": {\"a:b\": \"on\", \"c:d\": true}}");
        assertEquals(false, reading.preset().lakes());
        assertEquals(Preset.DEFAULT_BIOME, reading.preset().biome());
        assertEquals(Map.of("c:d", true), reading.preset().options());
        assertEquals(3, reading.problems().size());
    }

    @Test
    void ignoresKeysOfALaterReleaseAndSaysItIsNewer() {
        PresetReading reading = PresetJson.read("{\"format\": 2, \"layers\": [], \"sky\": \"purple\"}");
        assertEquals(List.of("written by a later Flatgen (format 2), so what is new in it was left out"),
                reading.problems());
    }

    @Test
    void keepsAnEmptyStructureListApartFromNone() {
        Preset none = PresetJson.read("{\"layers\": [], \"structures\": []}").preset();
        Preset biomeAllows = PresetJson.read("{\"layers\": []}").preset();
        assertEquals(Optional.of(List.of()), none.structureSets());
        assertEquals(Optional.empty(), biomeAllows.structureSets());
        assertEquals(none, PresetJson.read(PresetJson.write(none)).preset());
    }

    @Test
    void refusesTextThatIsNotAJsonObject() {
        assertThrows(IllegalArgumentException.class, () -> PresetJson.read("layers: stone"));
        assertThrows(IllegalArgumentException.class, () -> PresetJson.read("[]"));
    }
}
