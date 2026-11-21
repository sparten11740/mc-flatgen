package dev.flatgen.structures.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The game's Generate Structures switch as Flatgen leaves it. */
class GenerateStructuresTest {
    private static final Optional<List<String>> NONE = Optional.of(List.of());
    private static final Optional<List<String>> BIOME = Optional.empty();
    private static final Optional<List<String>> VILLAGES = Optional.of(List.of("minecraft:villages"));
    private static final GenerateStructures ON = new GenerateStructures(true, false);
    private static final GenerateStructures OFF_BY_PLAYER = new GenerateStructures(false, false);
    private static final GenerateStructures OFF_BY_FLATGEN = new GenerateStructures(false, true);

    @Test
    void aPresetWithNoStructuresTurnsTheSwitchOff() {
        assertEquals(OFF_BY_FLATGEN, ON.after(NONE));
        assertEquals(OFF_BY_FLATGEN, OFF_BY_FLATGEN.after(NONE));
    }

    @Test
    void aSwitchThePlayerTurnedOffStaysTheirs() {
        assertEquals(OFF_BY_PLAYER, OFF_BY_PLAYER.after(NONE));
        assertEquals(OFF_BY_PLAYER, OFF_BY_PLAYER.after(BIOME));
        assertEquals(OFF_BY_PLAYER, OFF_BY_PLAYER.withdrawn());
    }

    @Test
    void structuresAgainTurnItBackOnOnlyIfFlatgenTurnedItOff() {
        assertEquals(ON, OFF_BY_FLATGEN.after(BIOME));
        assertEquals(ON, OFF_BY_FLATGEN.after(VILLAGES));
        assertEquals(ON, ON.after(VILLAGES));
    }

    @Test
    void leavingFlatgenTurnsBackOnWhatItTurnedOff() {
        assertEquals(ON, OFF_BY_FLATGEN.withdrawn());
        assertEquals(ON, ON.withdrawn());
    }
}
