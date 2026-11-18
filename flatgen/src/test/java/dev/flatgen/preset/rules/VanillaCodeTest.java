package dev.flatgen.preset.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The game's superflat code, written as its presets screen writes it. */
class VanillaCodeTest {
    @Test
    void writesLayersBottomUpWithHeightsAboveOneThenTheBiome() {
        assertEquals("minecraft:bedrock,100*minecraft:stone,2*minecraft:dirt,minecraft:grass_block;minecraft:plains",
                VanillaCode.write(TestPresets.TALL_FLAT));
    }

    @Test
    void writesAnEmptyWorldAsItsBiomeAlone() {
        Preset empty = new Preset("", "", List.of(), "minecraft:the_void", Optional.empty(), false, false, Map.of());
        assertEquals(";minecraft:the_void", VanillaCode.write(empty));
    }
}
