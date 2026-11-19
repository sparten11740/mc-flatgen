package dev.flatgen.library.rules;

import dev.flatgen.preset.rules.Preset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * The player's saved presets (F11), as files of config/flatgen/presets: their order, the names of new ones and the
 * files they are kept in.
 */
public final class PresetLibrary {
    /** What a file's name is made of: its preset's name in lower case, letters and digits, words joined by this. */
    private static final String WORD_JOIN = "_";
    private static final String JSON = ".json";
    /** The name a preset gets when it has none. */
    private static final String UNNAMED = "preset";

    private PresetLibrary() {
    }

    /**
     * A file of the library.
     *
     * @param file     its name within the folder, such as {@code tall_flat.json}
     * @param preset   the preset it holds; none when it cannot be read at all
     * @param problems what in it could not be read, for a warning on its card and the log
     */
    public record Entry(String file, Optional<Preset> preset, List<String> problems) {
        public Entry {
            problems = List.copyOf(problems);
        }

        /** Its preset's name, or its file's when it cannot be read. */
        public String name() {
            return preset.map(Preset::name).filter(name -> !name.isBlank()).orElse(file);
        }
    }

    /**
     * The entries in the order the library shows them: favourites first, then those used lately, the latest first,
     * then the rest by name.
     *
     * @param favourites the files of the favourite presets (F12)
     * @param recent     the files of the presets used lately, the latest first
     */
    public static List<Entry> ordered(List<Entry> entries, Set<String> favourites, List<String> recent) {
        Comparator<Entry> byName = Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT));
        List<Entry> ordered = new ArrayList<>(entries.stream().filter(entry -> favourites.contains(entry.file()))
                .sorted(byName).toList());
        recent.forEach(file -> entries.stream()
                .filter(entry -> entry.file().equals(file) && !ordered.contains(entry)).forEach(ordered::add));
        entries.stream().filter(entry -> !ordered.contains(entry)).sorted(byName).forEach(ordered::add);
        return ordered;
    }

    /** A name for a new preset not taken yet: the base, or else the base with the first free number, "Preset 2". */
    public static String newName(String base, Collection<String> taken) {
        String name = base;
        for (int number = 2; taken.contains(name); number++) {
            name = base + " " + number;
        }
        return name;
    }

    /**
     * A file name for a preset's name not taken yet: its words in plain lower case letters joined by underscores, as
     * {@code tall_flat.json} ("Überwelt" becomes {@code uberwelt.json}), with the first free number when taken, as
     * {@code tall_flat_2.json}.
     */
    public static String fileName(String presetName, Collection<String> takenFiles) {
        String plainLetters = Normalizer.normalize(presetName, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String words = plainLetters.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", WORD_JOIN)
                .replaceAll("^" + WORD_JOIN + "+|" + WORD_JOIN + "+$", "");
        String base = words.isEmpty() ? UNNAMED : words;
        String file = base + JSON;
        for (int number = 2; takenFiles.contains(file); number++) {
            file = base + WORD_JOIN + number + JSON;
        }
        return file;
    }
}
