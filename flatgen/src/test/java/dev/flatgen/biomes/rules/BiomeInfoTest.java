package dev.flatgen.biomes.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** What the biome picker shows of a biome. */
class BiomeInfoTest {
    private static final BiomeInfo PLAINS = new BiomeInfo("minecraft:plains", 0x91BD59, 0x77AB2F, 0x3F76E4, 0x78A7FF);

    @Test
    void showsGrassLeavesWaterAndSkyOpaque() {
        assertEquals(List.of(0xFF91BD59, 0xFF77AB2F, 0xFF3F76E4, 0xFF78A7FF), PLAINS.swatches());
    }

    @Test
    void comesFromTheNamespaceOfItsId() {
        assertEquals("minecraft", PLAINS.namespace());
        assertEquals("biomesoplenty", new BiomeInfo("biomesoplenty:lavender_field", 0, 0, 0, 0).namespace());
    }
}
