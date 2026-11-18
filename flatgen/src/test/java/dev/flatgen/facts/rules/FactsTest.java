package dev.flatgen.facts.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.biomes.rules.BiomeInfo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** What each world fact shows, and the lines it draws across the column. */
class FactsTest {
    private static final BiomeInfo.Colours COLOURS = new BiomeInfo.Colours(0, 0, 0, 0);
    private static final BiomeInfo PLAINS = biome(0.8f, true, true);
    private static final BiomeInfo DESERT = biome(2.0f, false, true);
    private static final BiomeInfo WINDSWEPT_HILLS = biome(0.2f, true, true);
    private static final BiomeInfo MUSHROOM_FIELDS = biome(0.9f, true, false);

    @Test
    void showsTheClassicFlatWorldsSurfaceRainSlimesAndMonsters() {
        Situation classic = Situation.of(PLAINS, -60, true);
        assertEquals(List.of(
                new Fact.Shown("minecraft:grass_block", false, List.of(new Fact.Words("flatgen.facts.surface",
                        List.of(-60)))),
                new Fact.Shown("minecraft:water_bucket", false, List.of(Fact.Words.of("flatgen.facts.weather.rain"))),
                new Fact.Shown("minecraft:slime_ball", false, List.of(new Fact.Words("flatgen.facts.slime_chunks",
                        List.of(40)))),
                new Fact.Shown("minecraft:zombie_head", false, List.of(Fact.Words.of("flatgen.facts.monsters")))),
                Facts.ALL.stream().map(fact -> fact.show(classic)).toList());
    }

    @Test
    void strikesThroughWhatDoesNotHappen() {
        Situation mushrooms = Situation.of(MUSHROOM_FIELDS, 60, true);
        assertEquals(List.of(true, true), Facts.ALL.subList(2, 4).stream()
                .map(fact -> fact.show(mushrooms).no()).toList());
        assertEquals("minecraft:sunflower", Facts.ALL.get(1).show(Situation.of(DESERT, 60, true)).icon());
    }

    @Test
    void drawsTheSlimeLineWhereSlimesSpawnAndTheSnowLineWhereSnowStarts() {
        Fact slimes = Facts.ALL.get(2);
        assertEquals(Optional.of(new Fact.Marker(40, 0xFF7FD34F)), slimes.marker(Situation.of(PLAINS, 60, true)));
        assertEquals(Optional.empty(), slimes.marker(Situation.of(PLAINS, 60, false)));
        assertEquals(List.of(), markers(Situation.of(DESERT, 60, false)));
        assertEquals(List.of(new Fact.Marker(120, 0xFF8FD8FF), new Fact.Marker(40, 0xFF7FD34F)),
                markers(Situation.of(WINDSWEPT_HILLS, 60, true)));
        assertEquals(Optional.empty(), Facts.ALL.get(0).marker(Situation.of(PLAINS, 60, true)));
    }

    private static List<Fact.Marker> markers(Situation situation) {
        return Facts.ALL.stream().flatMap(fact -> fact.marker(situation).stream()).toList();
    }

    private static BiomeInfo biome(float temperature, boolean precipitation, boolean monsters) {
        return new BiomeInfo("test:biome", COLOURS, new BiomeInfo.Climate(temperature, precipitation),
                new BiomeInfo.Spawns(monsters, monsters, false));
    }
}
