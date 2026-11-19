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
    private static final PresetLibrary.Entry WATER = entry("water.json", "Water World");
    private static final PresetLibrary.Entry TALL = entry("tall.json", "tall flat");
    private static final PresetLibrary.Entry SKY = entry("sky.json", "Sky Grid");
    private static final PresetLibrary.Entry BROKEN = new PresetLibrary.Entry("broken.json", Optional.empty(),
            List.of("not JSON"));

    @Test
    void ordersFavouritesThenTheLatestUsedThenByName() {
        assertEquals(List.of(SKY, WATER, BROKEN, TALL), PresetLibrary.ordered(List.of(WATER, TALL, SKY, BROKEN),
                Set.of("sky.json"), List.of("water.json", "sky.json", "water.json")));
        assertEquals(List.of(BROKEN, SKY, TALL, WATER), PresetLibrary.ordered(List.of(WATER, TALL, SKY, BROKEN),
                Set.of(), List.of()));
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
        assertEquals("preset.json", PresetLibrary.fileName("!!!", Set.of()));
        assertEquals("uberwelt.json", PresetLibrary.fileName("Überwelt", Set.of()));
    }

    private static PresetLibrary.Entry entry(String file, String name) {
        Preset preset = TestPresets.TALL_FLAT.withName(name);
        return new PresetLibrary.Entry(file, Optional.of(preset), List.of());
    }
}
