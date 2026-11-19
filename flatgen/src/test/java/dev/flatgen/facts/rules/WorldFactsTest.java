package dev.flatgen.facts.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.biomes.rules.BiomeInfo;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

/** What a world will be like at its surface, for the biomes F08 names, at the heights that matter. */
class WorldFactsTest {
    /** Where the climates of these tests cool from: 1.21.1's Y 80, which their heights are reckoned for. */
    private static final int COOLING_START_Y = 80;
    private static final BiomeInfo.Colours COLOURS = new BiomeInfo.Colours(0, 0, 0, 0);
    private static final BiomeInfo PLAINS = biome(0.8f, true, true, true, false);
    private static final BiomeInfo DESERT = biome(2.0f, false, true, true, false);
    private static final BiomeInfo SNOWY_PLAINS = biome(0.0f, true, true, true, false);
    private static final BiomeInfo SWAMP = biome(0.8f, true, true, true, true);
    private static final BiomeInfo MUSHROOM_FIELDS = biome(0.9f, true, false, false, false);
    private static final BiomeInfo WINDSWEPT_HILLS = biome(0.2f, true, true, true, false);

    @Test
    void slimesSpawnInSlimeChunksBelowFortyOnly() {
        assertEquals(true, WorldFacts.of(PLAINS, 39, true).slimeChunks());
        assertEquals(false, WorldFacts.of(PLAINS, 40, true).slimeChunks());
        assertEquals(false, WorldFacts.of(MUSHROOM_FIELDS, 39, true).slimeChunks());
        assertEquals(false, WorldFacts.of(PLAINS, 39, false).slimeChunks());
    }

    @Test
    void slimesSpawnOnASwampsSurfaceBetweenFiftyOneAndSixtyNine() {
        assertEquals(false, WorldFacts.of(SWAMP, 40, true).surfaceSlimes());
        assertEquals(true, WorldFacts.of(SWAMP, 60, true).surfaceSlimes());
        assertEquals(false, WorldFacts.of(SWAMP, 100, true).surfaceSlimes());
        assertEquals(false, WorldFacts.of(PLAINS, 60, true).surfaceSlimes());
    }

    @Test
    void itRainsSnowsOrNeitherByTheBiomeAndTheHeight() {
        assertEquals(WorldFacts.Weather.RAIN, WorldFacts.of(PLAINS, 100, true).weather());
        assertEquals(WorldFacts.Weather.NONE, WorldFacts.of(DESERT, 60, true).weather());
        assertEquals(WorldFacts.Weather.SNOW, WorldFacts.of(SNOWY_PLAINS, 39, true).weather());
        assertEquals(WorldFacts.Weather.RAIN, WorldFacts.of(WINDSWEPT_HILLS, 100, true).weather());
        assertEquals(WorldFacts.Weather.SNOW, WorldFacts.of(WINDSWEPT_HILLS, 150, true).weather());
    }

    @Test
    void saysWhereSnowStartsWhenTheSurfaceIsNearIt() {
        assertEquals(OptionalInt.of(120), WorldFacts.snowLine(WINDSWEPT_HILLS.climate()));
        assertEquals(OptionalInt.of(120), WorldFacts.of(WINDSWEPT_HILLS, 114, true).snowLine());
        assertEquals(OptionalInt.empty(), WorldFacts.of(WINDSWEPT_HILLS, 100, true).snowLine());
        assertEquals(OptionalInt.empty(), WorldFacts.snowLine(SNOWY_PLAINS.climate()));
        assertEquals(OptionalInt.empty(), WorldFacts.snowLine(DESERT.climate()));
    }

    @Test
    void monstersSpawnWhereTheBiomeHasThemAndMobSpawningIsOn() {
        assertEquals(true, WorldFacts.of(PLAINS, 60, true).hostileMobs());
        assertEquals(false, WorldFacts.of(MUSHROOM_FIELDS, 60, true).hostileMobs());
        assertEquals(false, WorldFacts.of(PLAINS, 60, false).hostileMobs());
    }

    private static BiomeInfo biome(float temperature, boolean precipitation, boolean monsters, boolean slimes,
            boolean surfaceSlimes) {
        return new BiomeInfo("test:biome", COLOURS, new BiomeInfo.Climate(temperature, precipitation, COOLING_START_Y),
                new BiomeInfo.Spawns(monsters, slimes, surfaceSlimes));
    }
}
