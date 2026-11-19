package dev.flatgen.library;

import dev.flatgen.Flatgen;
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

    private PresetImports() {
    }

    /** Why something was left out of an import, each with the translation key of the words that tell the player. */
    public enum Reason {
        /** A dropped file whose name does not end as a preset file's does. */
        NOT_A_PRESET_FILE("flatgen.library.skipped.not_a_preset_file"),
        /** A file the game could not read. */
        UNREADABLE("flatgen.library.skipped.unreadable"),
        /** Text that is neither a preset nor a superflat code. */
        NOT_A_PRESET("flatgen.library.skipped.not_a_preset"),
        /** A preset that could not be written to the player's folder. */
        NOT_WRITTEN("flatgen.library.skipped.not_written");

        private final String key;

        Reason(String key) {
            this.key = key;
        }

        /** The translation key of the words that tell the player. */
        public String key() {
            return key;
        }
    }

    /**
     * Something left out of an import.
     *
     * @param file   the dropped file's name; none for pasted text
     * @param reason why it was left out
     */
    public record Skipped(Optional<String> file, Reason reason) {
    }

    /**
     * What an import did.
     *
     * @param imported the names of the presets imported
     * @param skipped  what was left out, each with why
     */
    public record Outcome(List<String> imported, List<Skipped> skipped) {
        public Outcome {
            imported = List.copyOf(imported);
            skipped = List.copyOf(skipped);
        }
    }

    /** Imports the preset or superflat code a text holds. */
    public static Outcome fromText(String text, PresetStore store) {
        List<String> imported = new ArrayList<>();
        List<Skipped> skipped = new ArrayList<>();
        add(PresetText.read(text), Optional.empty(), store, imported, skipped);
        return new Outcome(imported, skipped);
    }

    /** Imports each preset file dropped; a file that is not one is left out. */
    public static Outcome fromFiles(List<Path> files, PresetStore store) {
        List<String> imported = new ArrayList<>();
        List<Skipped> skipped = new ArrayList<>();
        for (Path file : files) {
            Optional<String> name = Optional.of(file.getFileName().toString());
            if (!name.get().toLowerCase(Locale.ROOT).endsWith(PresetLibrary.FILE_ENDING)) {
                skipped.add(new Skipped(name, Reason.NOT_A_PRESET_FILE));
                continue;
            }
            try {
                add(PresetText.read(Files.readString(file)), name, store, imported, skipped);
            } catch (IOException exception) {
                Flatgen.LOGGER.warn("Could not read {} to import it", file, exception);
                skipped.add(new Skipped(name, Reason.UNREADABLE));
            }
        }
        return new Outcome(imported, skipped);
    }

    private static void add(PresetText.Reading reading, Optional<String> file, PresetStore store,
            List<String> imported, List<Skipped> skipped) {
        if (reading.preset().isEmpty()) {
            Flatgen.LOGGER.info("Left {} out of the import: {}", file.orElse("the pasted text"), reading.problems());
            skipped.add(new Skipped(file, Reason.NOT_A_PRESET));
            return;
        }
        Preset preset = reading.preset().get();
        List<String> names = store.load().stream().map(PresetLibrary.Entry::name).toList();
        String base = preset.name().isBlank() ? PresetText.PASTED_NAME : preset.name();
        Preset named = preset.withName(PresetLibrary.newName(base, names));
        if (store.saveNew(named).isPresent()) {
            imported.add(named.name());
        } else {
            skipped.add(new Skipped(file, Reason.NOT_WRITTEN));
        }
    }
}
