package dev.flatgen.library.rules;

import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.preset.rules.PresetReading;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A preset shared as text (F15), as players pass them around: a preset file's JSON, or the game's superflat code
 * (F06), read into a preset for the library.
 */
public final class PresetText {
    /** The name a preset read from a superflat code gets, numbered when taken. */
    public static final String PASTED_NAME = "Pasted";
    private static final String JSON_START = "{";

    private PresetText() {
    }

    /**
     * Text read as a preset.
     *
     * @param preset   the preset; none when the text is neither a preset nor a code
     * @param problems what could not be read
     */
    public record Reading(Optional<Preset> preset, List<String> problems) {
        public Reading {
            problems = List.copyOf(problems);
        }
    }

    /** Reads a preset file's JSON or a superflat code; anything else is no preset, saying why. */
    public static Reading read(String text) {
        String trimmed = text.strip();
        if (trimmed.startsWith(JSON_START)) {
            try {
                PresetReading reading = PresetJson.read(trimmed);
                return new Reading(Optional.of(reading.preset()), reading.problems());
            } catch (IllegalArgumentException exception) {
                return new Reading(Optional.empty(), List.of("the text is " + exception.getMessage()));
            }
        }
        VanillaCode.Reading code = VanillaCode.read(trimmed);
        if (!code.problems().isEmpty()) {
            return new Reading(Optional.empty(), List.of("the text is neither a preset nor a superflat code"));
        }
        List<Layer> layers = code.layers();
        String icon = layers.isEmpty() ? "" : layers.get(layers.size() - 1).block();
        return new Reading(Optional.of(new Preset(PASTED_NAME, icon, layers, code.biome(), Optional.empty(), false,
                false, Map.of())), List.of());
    }
}
