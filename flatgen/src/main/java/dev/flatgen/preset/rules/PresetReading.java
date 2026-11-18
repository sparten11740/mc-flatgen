package dev.flatgen.preset.rules;

import java.util.List;

/**
 * A preset read from JSON, and what in the JSON could not be read and was left at its default or out.
 *
 * @param preset   the preset as far as it could be read
 * @param problems each thing that could not be read, for the log or the player
 */
public record PresetReading(Preset preset, List<String> problems) {
    public PresetReading {
        problems = List.copyOf(problems);
    }
}
