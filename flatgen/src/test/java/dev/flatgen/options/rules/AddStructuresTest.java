package dev.flatgen.options.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Structure sets that options add to a preset's chosen list. */
class AddStructuresTest {
    private static final Set<String> KNOWN = Set.of("minecraft:villages", "skyliners:airports");

    @Test
    void addsEachSetOnceToAChosenList() {
        Preset villages = TestPresets.TALL_FLAT.withStructureSets(Optional.of(List.of("minecraft:villages")));
        assertEquals(Optional.of(List.of("minecraft:villages", "skyliners:airports")), AddStructures.addedTo(villages,
                List.of("skyliners:airports", "minecraft:villages"), KNOWN).structureSets());
    }

    @Test
    void leavesOutSetsTheGameLacksSoTheListStaysAList() {
        Preset villages = TestPresets.TALL_FLAT.withStructureSets(Optional.of(List.of("minecraft:villages")));
        assertEquals(Optional.of(List.of("minecraft:villages")),
                AddStructures.addedTo(villages, List.of("mypack:towers"), KNOWN).structureSets());
    }

    @Test
    void takesBackOnlyWhatOptionsAloneAdded() {
        Preset villages = TestPresets.TALL_FLAT.withStructureSets(Optional.of(List.of("minecraft:villages")));
        Set<String> newly = AddStructures.newlyAdded(villages, List.of("skyliners:airports", "minecraft:villages"),
                KNOWN);
        assertEquals(Set.of("skyliners:airports"), newly);
        Preset applied = AddStructures.addedTo(villages, List.of("skyliners:airports"), KNOWN);
        assertEquals(villages, AddStructures.removedFrom(applied, newly));
    }

    @Test
    void leavesAPresetTakingWhatItsBiomeAllows() {
        Preset everything = TestPresets.TALL_FLAT.withStructureSets(Optional.empty());
        assertEquals(everything, AddStructures.addedTo(everything, List.of("skyliners:airports"), KNOWN));
    }
}
