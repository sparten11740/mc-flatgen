package dev.flatgen.library.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** What becomes of the parts a saved preset names that this game lacks. */
class MissingPartsTest {
    @Test
    void replacesMissingBlocksWithStoneAndAMissingBiomeWithPlainsAndLeavesOutMissingSets() {
        Preset modded = TestPresets.TALL_FLAT.withLayers(List.of(new Layer("minecraft:bedrock", 1),
                new Layer("create:andesite_casing", 3))).withBiome("biomesoplenty:lavender_field")
                .withStructureSets(Optional.of(List.of("minecraft:villages", "idas:towers")));
        Preset replaced = MissingParts.replaced(modded, new MissingIds(List.of("create:andesite_casing"),
                List.of("biomesoplenty:lavender_field"), List.of("idas:towers")));
        assertEquals(List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:stone", 3)), replaced.layers());
        assertEquals("minecraft:plains", replaced.biome());
        assertEquals(Optional.of(List.of("minecraft:villages")), replaced.structureSets());
    }
}
