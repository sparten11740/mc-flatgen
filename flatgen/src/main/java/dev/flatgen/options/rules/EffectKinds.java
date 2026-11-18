package dev.flatgen.options.rules;

import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Every kind of effect, by the {@code type} an option's effect names it with; a new kind is one more entry. */
public final class EffectKinds {
    private static final Map<String, EffectReader> KINDS = Map.of(
            "game_rule", GameRule::read,
            "first_start_time", FirstStartTime::read,
            "structures", AddStructures::read,
            "place_at_spawn", PlaceAtSpawn::read);

    private EffectKinds() {
    }

    /** The effect a JSON object describes, by its type; none, with the problem noted, when it cannot be read. */
    public static Optional<Effect> read(JsonObject effect, List<String> problems) {
        String type = JsonFields.text(effect, "type", "", problems);
        EffectReader reader = KINDS.get(type);
        if (reader == null) {
            problems.add("no effect type \"" + type + "\"; this Flatgen knows " + String.join(", ",
                    KINDS.keySet().stream().sorted().toList()));
            return Optional.empty();
        }
        return reader.read(effect, problems);
    }
}
