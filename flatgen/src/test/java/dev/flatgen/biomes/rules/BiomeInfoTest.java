package dev.flatgen.biomes.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** What Flatgen shows of a biome. */
class BiomeInfoTest {
    private static final BiomeInfo.Colours PLAINS_COLOURS = new BiomeInfo.Colours(0x91BD59, 0x77AB2F, 0x3F76E4,
            0x78A7FF);

    @Test
    void showsGrassLeavesWaterAndSkyOpaque() {
        assertEquals(List.of(0xFF91BD59, 0xFF77AB2F, 0xFF3F76E4, 0xFF78A7FF), PLAINS_COLOURS.swatches());
    }

    @Test
    void comesFromTheNamespaceOfItsId() {
        BiomeInfo.Climate climate = new BiomeInfo.Climate(0.8f, true, 80);
        BiomeInfo.Spawns spawns = new BiomeInfo.Spawns(true, true, false);
        assertEquals("minecraft",
                new BiomeInfo("minecraft:plains", PLAINS_COLOURS, climate, spawns, List.of()).namespace());
        assertEquals("biomesoplenty",
                new BiomeInfo("biomesoplenty:lavender_field", PLAINS_COLOURS, climate, spawns, List.of()).namespace());
    }
}
