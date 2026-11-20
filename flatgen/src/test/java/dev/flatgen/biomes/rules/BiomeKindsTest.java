package dev.flatgen.biomes.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The biome picker's kinds, as the game's biomes and a mod's sort into them by tags and climate. */
class BiomeKindsTest {
    private static final BiomeInfo.Colours COLOURS = new BiomeInfo.Colours(0x91BD59, 0x77AB2F, 0x3F76E4, 0x78A7FF);
    private static final BiomeInfo.Spawns SPAWNS = new BiomeInfo.Spawns(true, false, false);
    private static final int COOLING_START_Y = 80;
    private static final String OVERWORLD = "minecraft:is_overworld";

    @Test
    void sortsTheGamesBiomesByTheirTagsAndClimate() {
        assertEquals(List.of("usual"), kinds(biome("minecraft:plains", 0.8F, true, OVERWORLD)));
        assertEquals(List.of("usual", "forests"),
                kinds(biome("minecraft:forest", 0.7F, true, OVERWORLD, "minecraft:is_forest")));
        assertEquals(List.of("usual", "dry"), kinds(biome("minecraft:desert", 2F, false, OVERWORLD)));
        assertEquals(List.of("snowy"), kinds(biome("minecraft:ice_spikes", 0F, true, OVERWORLD)));
        assertEquals(List.of("usual", "swamps"),
                kinds(biome("minecraft:swamp", 0.8F, true, OVERWORLD, "minecraft:allows_surface_slime_spawns")));
        assertEquals(List.of("caves"), kinds(biome("minecraft:lush_caves", 0.5F, true, OVERWORLD)));
        assertEquals(List.of("nether"),
                kinds(biome("minecraft:basalt_deltas", 2F, false, "minecraft:is_nether")));
    }

    @Test
    void sortsAModsBiomesByTheGamesTags() {
        assertEquals(List.of("water"),
                kinds(biome("somemod:coral_lagoon", 0.9F, true, OVERWORLD, "minecraft:is_ocean")));
        assertEquals(List.of("caves"), kinds(biome("somemod:crystal_cave", 0.5F, true, "c:is_cave")));
    }

    @Test
    void aBiomeOfNoKindIsOfNone() {
        assertEquals(List.of(), kinds(biome("somemod:odd", 0.5F, true)));
    }

    @Test
    void sortsAModsSwampsAndCavesByTheTagsOf1201Too() {
        assertEquals(List.of("swamps"), kinds(biome("somemod:bog", 0.8F, true, "forge:is_swamp")));
        assertEquals(List.of("caves"), kinds(biome("somemod:grotto", 0.5F, true, "c:caves")));
    }

    @Test
    void paintsABiomeAsTheFirstOfItsKindsWithAScene() {
        assertEquals(BiomeKinds.Scene.PLAIN, BiomeKinds.scene(biome("minecraft:plains", 0.8F, true, OVERWORLD)));
        BiomeKinds.Scene forest = BiomeKinds.scene(biome("minecraft:forest", 0.7F, true, OVERWORLD,
                "minecraft:is_forest"));
        assertEquals(true, forest.tree());
        BiomeKinds.Scene ocean = BiomeKinds.scene(biome("minecraft:ocean", 0.5F, true, OVERWORLD,
                "minecraft:is_ocean"));
        assertEquals(100, ocean.water());
    }

    private static List<String> kinds(BiomeInfo biome) {
        return BiomeKinds.ALL.stream().filter(kind -> kind.holds().test(biome))
                .map(kind -> kind.name().substring(kind.name().lastIndexOf('.') + 1)).toList();
    }

    private static BiomeInfo biome(String id, float temperature, boolean precipitation, String... tags) {
        return new BiomeInfo(id, COLOURS, new BiomeInfo.Climate(temperature, precipitation, COOLING_START_Y), SPAWNS,
                List.of(tags));
    }
}
