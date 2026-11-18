package dev.flatgen.search.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Finding blocks and biomes by what a player types. */
class SearchTest {
    private static final Search.Entry STONE = new Search.Entry("minecraft:stone", "Stone", "minecraft");
    private static final Search.Entry COBBLESTONE =
            new Search.Entry("minecraft:cobblestone", "Cobblestone", "minecraft");
    private static final Search.Entry STONE_BRICKS = new Search.Entry("minecraft:stone_bricks", "Stone Bricks",
            "minecraft");
    private static final Search.Entry MOSSY = new Search.Entry("minecraft:mossy_stone_bricks", "Mossy Stone Bricks",
            "minecraft");
    private static final Search.Entry CASING = new Search.Entry("create:andesite_casing", "Andesite Casing", "create");
    private static final List<Search.Entry> ALL = List.of(COBBLESTONE, MOSSY, STONE_BRICKS, STONE, CASING);

    @Test
    void putsTheWholeIdFirstThenNamesStartingWithTheTextThenWordsThenAnywhere() {
        assertEquals(List.of(STONE, STONE_BRICKS, MOSSY, COBBLESTONE), Search.find(ALL, "stone"));
    }

    @Test
    void findsByIdAsWellAsName() {
        assertEquals(List.of(CASING), Search.find(ALL, "create:"));
        assertEquals(List.of(CASING), Search.find(ALL, "andesite_c"));
    }

    @Test
    void ignoresCaseAndSpaceAround() {
        assertEquals(List.of(CASING), Search.find(ALL, "  ANDESITE "));
    }

    @Test
    void keepsTheGivenOrderForNoText() {
        assertEquals(ALL, Search.find(ALL, ""));
    }

    @Test
    void findsNothingForTextNothingHolds() {
        assertEquals(List.of(), Search.find(ALL, "netherite"));
    }
}
