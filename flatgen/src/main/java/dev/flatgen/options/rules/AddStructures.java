package dev.flatgen.options.rules;

import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@code structures}: adds structure sets to the world's chosen list of them.
 *
 * @param sets the structure sets' ids, such as {@code skyliners:airports}
 */
public record AddStructures(List<String> sets) implements Effect {
    public AddStructures {
        sets = List.copyOf(sets);
    }

    /** Reads {@code {"sets": ["skyliners:airports"]}}. */
    static Optional<Effect> read(JsonObject effect, List<String> problems) {
        List<String> sets = JsonFields.texts(effect, "sets", problems);
        if (sets.isEmpty()) {
            problems.add("a structures effect needs a list of structure set ids");
            return Optional.empty();
        }
        return Optional.of(new AddStructures(sets));
    }

    @Override
    public void atDone(WorldSetup setup) {
        setup.addStructureSets(sets);
    }

    /**
     * A preset with the structure sets added to its chosen list that the game has, each once; a preset taking what its
     * biome allows already has every set, so it stays as it is. A set the game lacks is left out, as the game would
     * read a list naming it as no list at all and generate every set.
     *
     * @param known the ids of the structure sets the game has
     */
    public static Preset addedTo(Preset preset, Collection<String> added, Set<String> known) {
        if (preset.structureSets().isEmpty() || added.isEmpty()) {
            return preset;
        }
        Set<String> sets = new LinkedHashSet<>(preset.structureSets().get());
        added.stream().filter(known::contains).forEach(sets::add);
        return preset.withStructureSets(Optional.of(new ArrayList<>(sets)));
    }
}
