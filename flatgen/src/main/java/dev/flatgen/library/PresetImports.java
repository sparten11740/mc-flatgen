package dev.flatgen.library;

import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.library.rules.PresetText;
import dev.flatgen.preset.rules.Preset;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Bringing presets into the player's folder (F15): pasted as text, or files dropped on Flatgen's screens. A name
 * already taken gets a number; nothing is overwritten.
 */
public final class PresetImports {
    private static final String JSON = ".json";

    private PresetImports() {
    }

    /**
     * What an import did.
     *
     * @param imported the names of the presets imported
     * @param skipped  what was left out, each with why
     */
    public record Outcome(List<String> imported, List<String> skipped) {
        public Outcome {
            imported = List.copyOf(imported);
            skipped = List.copyOf(skipped);
        }
    }

    /** Imports the preset or superflat code a text holds. */
    public static Outcome fromText(String text, PresetStore store) {
        List<String> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        add(PresetText.read(text), "", store, imported, skipped);
        return new Outcome(imported, skipped);
    }

    /** Imports each preset file dropped; a file that is not one is left out. */
    public static Outcome fromFiles(List<Path> files, PresetStore store) {
        List<String> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (Path file : files) {
            String name = file.getFileName().toString();
            if (!name.toLowerCase(Locale.ROOT).endsWith(JSON)) {
                skipped.add(name + ": not a preset file");
                continue;
            }
            try {
                add(PresetText.read(Files.readString(file)), name + ": ", store, imported, skipped);
            } catch (IOException exception) {
                skipped.add(name + ": " + exception.getMessage());
            }
        }
        return new Outcome(imported, skipped);
    }

    private static void add(PresetText.Reading reading, String from, PresetStore store, List<String> imported,
            List<String> skipped) {
        if (reading.preset().isEmpty()) {
            skipped.add(from + String.join("; ", reading.problems()));
            return;
        }
        Preset preset = reading.preset().get();
        List<String> names = store.load().stream().map(PresetLibrary.Entry::name).toList();
        String base = preset.name().isBlank() ? PresetText.PASTED_NAME : preset.name();
        Preset named = preset.withName(PresetLibrary.newName(base, names));
        Optional<String> saved = store.saveNew(named);
        if (saved.isPresent()) {
            imported.add(named.name());
        } else {
            skipped.add(from + "could not be written");
        }
    }
}
