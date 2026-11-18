package dev.flatgen.preset.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The game's superflat code, written as its presets screen writes it and read as it reads it. */
class VanillaCodeTest {
    private static final String TALL_FLAT =
            "minecraft:bedrock,100*minecraft:stone,2*minecraft:dirt,minecraft:grass_block;minecraft:plains";

    @Test
    void writesLayersBottomUpWithHeightsAboveOneThenTheBiome() {
        assertEquals(TALL_FLAT, VanillaCode.write(TestPresets.TALL_FLAT));
    }

    @Test
    void writesAnEmptyWorldAsItsBiomeAlone() {
        Preset empty = new Preset("", "", List.of(), "minecraft:the_void", Optional.empty(), false, false, Map.of());
        assertEquals(";minecraft:the_void", VanillaCode.write(empty));
    }

    @Test
    void readsWhatItWrites() {
        VanillaCode.Reading reading = VanillaCode.read(TALL_FLAT);
        assertEquals(TestPresets.TALL_FLAT.layers(), reading.layers());
        assertEquals("minecraft:plains", reading.biome());
        assertEquals(List.of(), reading.problems());
    }

    @Test
    void takesPlainsWhenTheBiomeIsLeftOutAndIgnoresSpaces() {
        VanillaCode.Reading reading = VanillaCode.read(" minecraft:bedrock , 3 * minecraft:dirt ");
        assertEquals(List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:dirt", 3)), reading.layers());
        assertEquals(Preset.DEFAULT_BIOME, reading.biome());
        assertEquals(List.of(), reading.problems());
    }

    @Test
    void saysWhichLayerItCannotReadAndWhere() {
        VanillaCode.Reading reading = VanillaCode.read("minecraft:bedrock,x*minecraft:stone,0*minecraft:dirt,Stone");
        assertEquals(List.of(new Layer("minecraft:bedrock", 1)), reading.layers());
        assertEquals(List.of(
                new VanillaCode.Problem(VanillaCode.Kind.UNREADABLE_LAYER, 18, 35, "x*minecraft:stone"),
                new VanillaCode.Problem(VanillaCode.Kind.UNREADABLE_LAYER, 36, 52, "0*minecraft:dirt"),
                new VanillaCode.Problem(VanillaCode.Kind.UNREADABLE_LAYER, 53, 58, "Stone")), reading.problems());
    }

    @Test
    void saysWhenThereAreNoLayers() {
        assertEquals(List.of(new VanillaCode.Problem(VanillaCode.Kind.NO_LAYERS, 0, 0, "")),
                VanillaCode.read(";minecraft:plains").problems());
        assertEquals(VanillaCode.Kind.NO_LAYERS, VanillaCode.read("").problems().get(0).kind());
    }
}
