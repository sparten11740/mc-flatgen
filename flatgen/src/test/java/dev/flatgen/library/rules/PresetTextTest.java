package dev.flatgen.library.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Presets shared as text: a file's JSON, a superflat code, or neither. */
class PresetTextTest {
    @Test
    void readsAPresetFilesJson() {
        PresetText.Reading reading = PresetText.read("  " + PresetJson.write(TestPresets.TALL_FLAT) + "\n");
        assertEquals(Optional.of(TestPresets.TALL_FLAT), reading.preset());
        assertEquals(List.of(), reading.problems());
    }

    @Test
    void readsASuperflatCodeAsAPastedPreset() {
        PresetText.Reading reading = PresetText.read("minecraft:bedrock,2*minecraft:sand;minecraft:desert");
        assertEquals(List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:sand", 2)),
                reading.preset().orElseThrow().layers());
        assertEquals("minecraft:desert", reading.preset().orElseThrow().biome());
        assertEquals(PresetText.PASTED_NAME, reading.preset().orElseThrow().name());
    }

    @Test
    void readsRubbishAsNoPresetSayingWhy() {
        assertEquals(Optional.empty(), PresetText.read("hello, world!").preset());
        assertTrue(PresetText.read("{ broken").problems().get(0).startsWith("the text is"));
        assertEquals(Optional.empty(), PresetText.read("").preset());
    }
}
