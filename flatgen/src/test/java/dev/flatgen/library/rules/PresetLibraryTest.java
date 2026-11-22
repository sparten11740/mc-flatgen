package dev.flatgen.library.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The order, names and files of the player's saved presets. */
class PresetLibraryTest {
    private static final PresetLibrary.Kind GAME = new PresetLibrary.Kind(1, false,
            Optional.of(PresetLibrary.BuiltIns.NO_GAME));
    private static final PresetLibrary.Kind PACK = new PresetLibrary.Kind(2, false,
            Optional.of(PresetLibrary.BuiltIns.HIDDEN));
    private static final PresetLibrary.Entry WATER = entry("water.json", "Water World");
    private static final PresetLibrary.Entry TALL = entry("tall.json", "tall flat");
    private static final PresetLibrary.Entry SKY = entry("sky.json", "Sky Grid");
    private static final PresetLibrary.Entry BROKEN = new PresetLibrary.Entry("broken.json",
            PresetLibrary.Kind.PLAYERS, Optional.empty(), List.of("not JSON"));
    private static final PresetLibrary.Entry CLASSIC = new PresetLibrary.Entry("game:minecraft:classic_flat",
            GAME, Optional.of(TestPresets.TALL_FLAT.withName("Classic Flat")), List.of());
    private static final PresetLibrary.Entry STARTER = new PresetLibrary.Entry("pack:flatgen:tall_flat",
            PACK, Optional.of(TestPresets.TALL_FLAT.withName("A tall flat")), List.of());

    @Test
    void ordersStarredThenTheLatestUsedThenByName() {
        assertEquals(List.of(SKY, WATER, BROKEN, TALL), PresetLibrary.ordered(List.of(WATER, TALL, SKY, BROKEN),
                Set.of("sky.json"), List.of("water.json", "sky.json", "water.json")));
        assertEquals(List.of(BROKEN, SKY, TALL, WATER), PresetLibrary.ordered(List.of(WATER, TALL, SKY, BROKEN),
                Set.of(), List.of()));
    }

    @Test
    void aBuiltInComesFromItsIdsNamespaceAndThePlayersOwnFromNone() {
        assertEquals(Optional.of("minecraft"), CLASSIC.namespace());
        assertEquals(Optional.of("flatgen"), STARTER.namespace());
        assertEquals(Optional.empty(), TALL.namespace());
    }

    @Test
    void ordersThePlayersOwnBeforeTheGamesAndThenPacks() {
        assertEquals(List.of(TALL, CLASSIC, STARTER), PresetLibrary.ordered(List.of(STARTER, CLASSIC, TALL), Set.of(),
                List.of()));
    }

    @Test
    void hidesTheGamesBuiltInsOrAll() {
        List<PresetLibrary.Entry> all = List.of(TALL, CLASSIC, STARTER);
        assertEquals(all, PresetLibrary.shown(all, PresetLibrary.BuiltIns.SHOWN));
        assertEquals(List.of(TALL, STARTER), PresetLibrary.shown(all, PresetLibrary.BuiltIns.NO_GAME));
        assertEquals(List.of(TALL), PresetLibrary.shown(all, PresetLibrary.BuiltIns.HIDDEN));
        assertEquals(false, CLASSIC.isPlayers());
    }

    @Test
    void namesABrokenFileAfterItself() {
        assertEquals("broken.json", BROKEN.name());
        assertEquals("Water World", WATER.name());
    }

    @Test
    void namesANewPresetWithTheFirstFreeNumber() {
        assertEquals("Preset", PresetLibrary.newName("Preset", List.of("Water World")));
        assertEquals("Preset 3", PresetLibrary.newName("Preset", List.of("Preset", "Preset 2")));
    }

    @Test
    void keepsAPresetInAFileNamedAfterIt() {
        assertEquals("tall_flat.json", PresetLibrary.fileName("Tall Flat!", Set.of()));
        assertEquals("tall_flat_2.json", PresetLibrary.fileName("tall flat", Set.of("tall_flat.json")));
        assertEquals("tall_flat_2.json", PresetLibrary.fileName("tall flat", Set.of("Tall_Flat.json")));
        assertEquals("preset.json", PresetLibrary.fileName("!!!", Set.of()));
        assertEquals("uberwelt.json", PresetLibrary.fileName("Überwelt", Set.of()));
    }

    @Test
    void namesAnExportAfterThePlayersFileOrTheBuiltInsName() {
        assertEquals("glass_2", PresetLibrary.exportName(entry("glass_2.json", "Glass")));
        assertEquals("classic_flat", PresetLibrary.exportName(CLASSIC));
        assertEquals("a_tall_flat", PresetLibrary.exportName(STARTER));
    }

    private static PresetLibrary.Entry entry(String file, String name) {
        Preset preset = TestPresets.TALL_FLAT.withName(name);
        return new PresetLibrary.Entry(file, PresetLibrary.Kind.PLAYERS, Optional.of(preset), List.of());
    }
}
