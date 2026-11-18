package dev.flatgen.options.rules;

import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.List;
import java.util.Optional;

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
}
