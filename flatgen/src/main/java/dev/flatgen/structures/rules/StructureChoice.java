package dev.flatgen.structures.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Which structure sets a preset generates, as its {@code structure_overrides}: what the biome allows (no list), none
 * (an empty list) or a chosen list. A list may also hold a tag ({@code #namespace:path}), which is kept as it is.
 */
public final class StructureChoice {
    private StructureChoice() {
    }

    /** The three ways a preset chooses its structure sets. */
    public enum Mode {
        /** Every structure set the biome allows, as vanilla's presets without overrides. */
        BIOME,
        /** No structures at all. */
        NONE,
        /** The sets in the list only. */
        CHOSEN
    }

    /** How a preset's structure sets are chosen. */
    public static Mode mode(Optional<List<String>> sets) {
        if (sets.isEmpty()) {
            return Mode.BIOME;
        }
        return sets.get().isEmpty() ? Mode.NONE : Mode.CHOSEN;
    }

    /**
     * The structure sets for a mode: none for the biome's, an empty list for none, and for a chosen list the one the
     * preset has, or every set when it had none chosen, the list closest to the world it had.
     */
    public static Optional<List<String>> withMode(Optional<List<String>> sets, Mode mode, List<String> every) {
        switch (mode) {
            case BIOME:
                return Optional.empty();
            case NONE:
                return Optional.of(List.of());
            default:
                return mode(sets) == Mode.CHOSEN ? sets : Optional.of(List.copyOf(every));
        }
    }

    /** Whether a set generates by this choice, as far as the choice goes; the biome decides the rest. */
    public static boolean isOn(Optional<List<String>> sets, String set) {
        return sets.map(list -> list.contains(set)).orElse(true);
    }

    /**
     * The structure sets once one is turned on or off: a preset taking the biome's starts from every set, and one left
     * with none chooses none.
     */
    public static Optional<List<String>> toggled(Optional<List<String>> sets, String set, List<String> every) {
        List<String> chosen = new ArrayList<>(sets.orElse(every));
        if (!chosen.remove(set)) {
            chosen.add(set);
        }
        return Optional.of(List.copyOf(chosen));
    }
}
