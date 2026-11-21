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
import java.util.stream.Collectors;

/**
 * The presets of the library: the player's own (F11), files of config/flatgen/presets, and the built-in ones (F13) of
 * the game, its data packs, mods and resource packs; their order, the names of new ones and the files they are kept in.
 */
public final class PresetLibrary {
    /** What a file's name is made of: its preset's name in lower case, letters and digits, words joined by this. */
    private static final String WORD_JOIN = "_";
    /** How the name of a preset file ends. */
    public static final String FILE_ENDING = ".json";
    /** The name a preset gets when it has none. */
    private static final String UNNAMED = "preset";
    /** The parts of a built-in's key: its source, then its id's namespace and path, as game:minecraft:classic_flat. */
    private static final int ID_PARTS = 3;

    private PresetLibrary() {
    }

    /**
     * What the presets of a source are like in the library, as each source says: a new source chooses its own, and
     * the library needs no change.
     *
     * @param rank       where they come in the library's order after the favourites and recent ones, lowest first
     * @param editable   whether the player may rename, save over and delete them, as their own
     * @param hiddenFrom the least of the built-ins settings that hides them; none when no setting does
     */
    public record Kind(int rank, boolean editable, Optional<BuiltIns> hiddenFrom) {
        /** The player's own presets, first and theirs to change. */
        public static final Kind PLAYERS = new Kind(0, true, Optional.empty());
    }

    /** Which built-in presets the library shows, each setting hiding more than the one before. */
    public enum BuiltIns {
        /** All of them. */
        SHOWN,
        /** Those of mods and packs, not the game's. */
        NO_GAME,
        /** None: only the player's own. */
        HIDDEN
    }

    /**
     * A preset of the library.
     *
     * @param key      how the library names it: a player's file name, such as {@code tall_flat.json}, or a built-in's
     *                 id, such as {@code game:minecraft:classic_flat}
     * @param kind     what its source's presets are like in the library
     * @param preset   the preset; none when it cannot be read at all
     * @param problems what in it could not be read, for a warning on its card and the log
     */
    public record Entry(String key, Kind kind, Optional<Preset> preset, List<String> problems) {
        public Entry {
            problems = List.copyOf(problems);
        }

        /** Its preset's name, or its key when it cannot be read. */
        public String name() {
            return preset.map(Preset::name).filter(name -> !name.isBlank()).orElse(key);
        }

        /** Whether it is the player's own, which they may rename, save over and delete; built-ins they may not. */
        public boolean isPlayers() {
            return kind.editable();
        }

        /**
         * The namespace of a built-in's id, the mod's or pack's it comes from, as {@code minecraft}; none for the
         * player's own.
         */
        public Optional<String> namespace() {
            String[] parts = key.split(":", ID_PARTS);
            return isPlayers() || parts.length < ID_PARTS ? Optional.empty() : Optional.of(parts[1]);
        }
    }

    /** The entries the library shows with these built-ins shown. */
    public static List<Entry> shown(List<Entry> entries, BuiltIns builtIns) {
        return entries.stream().filter(entry -> entry.kind().hiddenFrom()
                .map(from -> builtIns.ordinal() < from.ordinal()).orElse(true)).toList();
    }

    /**
     * The entries in the order the library shows them: favourites first, then those used lately, the latest first,
     * then the rest by their source's rank (the player's, the game's, then packs') and by name.
     *
     * @param favourites the keys of the favourite presets (F12)
     * @param recent     the keys of the presets used lately, the latest first
     */
    public static List<Entry> ordered(List<Entry> entries, Set<String> favourites, List<String> recent) {
        Comparator<Entry> byName = Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT));
        List<Entry> ordered = new ArrayList<>(entries.stream().filter(entry -> favourites.contains(entry.key()))
                .sorted(byName).toList());
        recent.forEach(key -> entries.stream()
                .filter(entry -> entry.key().equals(key) && !ordered.contains(entry)).forEach(ordered::add));
        entries.stream().filter(entry -> !ordered.contains(entry))
                .sorted(Comparator.comparingInt((Entry entry) -> entry.kind().rank()).thenComparing(byName))
                .forEach(ordered::add);
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
     * {@code tall_flat_2.json}. A name is taken whatever its case, as on the file systems that ignore it.
     */
    public static String fileName(String presetName, Collection<String> takenFiles) {
        Set<String> taken = takenFiles.stream().map(file -> file.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        String plainLetters = Normalizer.normalize(presetName, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String words = plainLetters.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", WORD_JOIN)
                .replaceAll("^" + WORD_JOIN + "+|" + WORD_JOIN + "+$", "");
        String base = words.isEmpty() ? UNNAMED : words;
        String file = base + FILE_ENDING;
        for (int number = 2; taken.contains(file); number++) {
            file = base + WORD_JOIN + number + FILE_ENDING;
        }
        return file;
    }

    /**
     * An export's name for a library entry, as a datapack's: a player's preset's file name without its ending, as
     * {@code glass_2}, or a built-in's name made into one, plain lower case letters, digits and underscores.
     */
    public static String exportName(Entry entry) {
        String base = entry.isPlayers() && entry.key().endsWith(FILE_ENDING)
                ? entry.key().substring(0, entry.key().length() - FILE_ENDING.length())
                : entry.name();
        String file = fileName(base, List.of());
        return file.substring(0, file.length() - FILE_ENDING.length());
    }
}
