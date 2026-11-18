package dev.flatgen.options.rules;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;

/** Reads one kind of effect from its JSON object, noting what is wrong; none when it cannot be read. */
@FunctionalInterface
public interface EffectReader {
    Optional<Effect> read(JsonObject effect, List<String> problems);
}
