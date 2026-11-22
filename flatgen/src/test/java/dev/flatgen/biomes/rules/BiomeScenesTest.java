package dev.flatgen.biomes.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** How biomes are painted: alike biomes told apart by their ground, their tree and what more they have. */
class BiomeScenesTest {
    private static final BiomeInfo.Colours COLOURS = new BiomeInfo.Colours(0x91BD59, 0x77AB2F, 0x3F76E4, 0x78A7FF);
    private static final BiomeInfo.Spawns SPAWNS = new BiomeInfo.Spawns(true, false, false);
    private static final int COOLING_START_Y = 80;
    private static final String OVERWORLD = "minecraft:is_overworld";

    @Test
    void tellsTheDryBiomesApartByTheirGround() {
        BiomeScenes.Scene desert = scene("minecraft:desert", 2F, false, OVERWORLD);
        BiomeScenes.Scene badlands = scene("minecraft:badlands", 2F, false, OVERWORLD, "minecraft:is_badlands");
        BiomeScenes.Scene savanna = scene("minecraft:savanna", 2F, false, OVERWORLD, "minecraft:is_savanna");
        assertNotEquals(desert.ground(), badlands.ground());
        assertNotEquals(desert.ground(), savanna.ground());
        assertNotEquals(List.of(), desert.patches());
        assertNotEquals(desert.patches(), savanna.patches());
        assertEquals(List.of(), badlands.patches());
    }

    @Test
    void givesTheBambooJungleBambooAndTheNetherLavaInItsGround() {
        BiomeScenes.Scene jungle = scene("minecraft:jungle", 0.95F, true, OVERWORLD, "minecraft:is_jungle");
        BiomeScenes.Scene bamboo = scene("minecraft:bamboo_jungle", 0.95F, true, OVERWORLD, "minecraft:is_jungle");
        assertNotEquals(jungle.patches(), bamboo.patches());
        BiomeScenes.Scene nether = scene("minecraft:nether_wastes", 2F, false, "minecraft:is_nether");
        assertTrue(nether.patches().stream().anyMatch(patch -> patch.bottom() < 0));
    }

    @Test
    void tellsTheForestsApartByTheirTrees() {
        BiomeScenes.Scene forest = scene("minecraft:forest", 0.7F, true, OVERWORLD, "minecraft:is_forest");
        BiomeScenes.Scene birch = scene("minecraft:birch_forest", 0.6F, true, OVERWORLD, "minecraft:is_forest");
        BiomeScenes.Scene taiga = scene("minecraft:taiga", 0.25F, true, OVERWORLD, "minecraft:is_taiga");
        BiomeScenes.Scene cherry = scene("minecraft:cherry_grove", 0.5F, true, OVERWORLD, "minecraft:is_mountain");
        assertNotEquals(forest.patches(), birch.patches());
        assertNotEquals(forest.patches(), taiga.patches());
        assertNotEquals(forest.patches(), cherry.patches());
    }

    @Test
    void paintsTheVoidAsDarkWithNoGroundAndASnowyTaigaWithSnowUnderItsSpruce() {
        BiomeScenes.Scene voidScene = scene("minecraft:the_void", 0.5F, false);
        assertEquals(BiomeScenes.NONE, voidScene.ground().body());
        assertNotEquals(BiomeScenes.OWN, voidScene.sky());
        BiomeScenes.Scene snowyTaiga = scene("minecraft:snowy_taiga", -0.5F, true, OVERWORLD, "minecraft:is_taiga");
        BiomeScenes.Scene taiga = scene("minecraft:taiga", 0.25F, true, OVERWORLD, "minecraft:is_taiga");
        assertEquals(taiga.patches(), snowyTaiga.patches());
        assertNotEquals(taiga.ground(), snowyTaiga.ground());
    }

    @Test
    void givesTheSeaItsWaterAndTheMushroomFieldsTheirMushroom() {
        assertEquals(100, scene("minecraft:ocean", 0.5F, true, OVERWORLD, "minecraft:is_ocean").water());
        BiomeScenes.Scene plains = scene("minecraft:plains", 0.8F, true, OVERWORLD);
        BiomeScenes.Scene mushroom = scene("minecraft:mushroom_fields", 0.9F, true, OVERWORLD);
        assertNotEquals(plains.ground(), mushroom.ground());
        assertNotEquals(plains.patches(), mushroom.patches());
    }

    @Test
    void paintsTheMushroomsSpotsWhiteAndAModsSwampOf1201AsASwamp() {
        BiomeScenes.Scene mushroom = scene("minecraft:mushroom_fields", 0.9F, true, OVERWORLD);
        assertTrue(mushroom.patches().stream().anyMatch(patch -> patch.colour() == 0xFFFFFFFF));
        BiomeScenes.Scene bog = scene("somemod:bog", 0.8F, true, OVERWORLD, "forge:is_swamp");
        assertEquals(scene("minecraft:swamp", 0.8F, true, OVERWORLD, "minecraft:allows_surface_slime_spawns"), bog);
    }

    private static BiomeScenes.Scene scene(String id, float temperature, boolean precipitation, String... tags) {
        return BiomeScenes.of(new BiomeInfo(id, COLOURS, new BiomeInfo.Climate(temperature, precipitation,
                COOLING_START_Y), SPAWNS, List.of(tags)));
    }
}
