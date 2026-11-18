package dev.flatgen.options.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Structure sets that options add to a preset's chosen list. */
class AddStructuresTest {
    @Test
    void addsEachSetOnceToAChosenList() {
        Preset villages = TestPresets.TALL_FLAT.withStructureSets(Optional.of(List.of("minecraft:villages")));
        assertEquals(Optional.of(List.of("minecraft:villages", "skyliners:airports")),
                AddStructures.addedTo(villages, List.of("skyliners:airports", "minecraft:villages")).structureSets());
    }

    @Test
    void leavesAPresetTakingWhatItsBiomeAllows() {
        Preset everything = TestPresets.TALL_FLAT.withStructureSets(Optional.empty());
        assertEquals(everything, AddStructures.addedTo(everything, List.of("skyliners:airports")));
    }
}
