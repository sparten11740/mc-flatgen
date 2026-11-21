package dev.flatgen.structures.rules;

import java.util.List;
import java.util.Optional;

/**
 * The game's Generate Structures switch on the Create World screen as Flatgen leaves it: off for a preset with no
 * structures, so that no dimension has any, the Nether's fortresses and the End's cities included, and on again once
 * a preset that Flatgen turned it off for gives way to one with structures. A switch the player turned off is theirs.
 *
 * @param on        whether the switch is on
 * @param byFlatgen whether Flatgen turned it off, and so turns it on again
 */
public record GenerateStructures(boolean on, boolean byFlatgen) {
    /** The switch after Done applies a preset with these structure sets. */
    public GenerateStructures after(Optional<List<String>> structureSets) {
        return StructureChoice.mode(structureSets) == StructureChoice.Mode.NONE
                ? new GenerateStructures(false, byFlatgen || on) : withdrawn();
    }

    /** The switch once Flatgen no longer edits the world: on again if Flatgen turned it off. */
    public GenerateStructures withdrawn() {
        return new GenerateStructures(on || byFlatgen, false);
    }
}
