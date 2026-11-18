package dev.flatgen.structures.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The three ways a preset chooses its structure sets, as structure_overrides. */
class StructureChoiceTest {
    private static final List<String> EVERY = List.of("minecraft:villages", "minecraft:strongholds",
            "minecraft:mineshafts");

    @Test
    void readsTheModeFromTheList() {
        assertEquals(StructureChoice.Mode.BIOME, StructureChoice.mode(Optional.empty()));
        assertEquals(StructureChoice.Mode.NONE, StructureChoice.mode(Optional.of(List.of())));
        assertEquals(StructureChoice.Mode.CHOSEN, StructureChoice.mode(Optional.of(List.of("minecraft:villages"))));
    }

    @Test
    void switchesModesKeepingAChosenList() {
        Optional<List<String>> villages = Optional.of(List.of("minecraft:villages"));
        assertEquals(Optional.empty(), StructureChoice.withMode(villages, StructureChoice.Mode.BIOME, EVERY));
        assertEquals(Optional.of(List.of()), StructureChoice.withMode(villages, StructureChoice.Mode.NONE, EVERY));
        assertEquals(villages, StructureChoice.withMode(villages, StructureChoice.Mode.CHOSEN, EVERY));
        assertEquals(Optional.of(EVERY), StructureChoice.withMode(Optional.empty(), StructureChoice.Mode.CHOSEN,
                EVERY));
    }

    @Test
    void togglesASetStartingFromEverySetWhenTheBiomeChose() {
        assertEquals(true, StructureChoice.isOn(Optional.empty(), "minecraft:villages"));
        Optional<List<String>> noVillages = StructureChoice.toggled(Optional.empty(), "minecraft:villages", EVERY);
        assertEquals(Optional.of(List.of("minecraft:strongholds", "minecraft:mineshafts")), noVillages);
        assertEquals(false, StructureChoice.isOn(noVillages, "minecraft:villages"));
        assertEquals(Optional.of(List.of("#minecraft:villages", "minecraft:mineshafts")), StructureChoice.toggled(
                Optional.of(List.of("#minecraft:villages")), "minecraft:mineshafts", EVERY));
        assertEquals(StructureChoice.Mode.NONE, StructureChoice.mode(StructureChoice.toggled(
                Optional.of(List.of("minecraft:villages")), "minecraft:villages", EVERY)));
    }
}
