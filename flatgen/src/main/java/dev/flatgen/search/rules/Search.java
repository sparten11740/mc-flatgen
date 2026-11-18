package dev.flatgen.search.rules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Finding things by what a player types, for the block and biome pickers alike: an entry matches when its id or its
 * name holds the text, and better matches come first (the whole id, a name starting with it, a word of the name
 * starting with it, then anywhere). An empty text matches everything in the order given.
 */
public final class Search {
    /** How well an entry matches, best first; an entry that does not match is left out. */
    private static final int WHOLE_ID = 0;
    private static final int NAME_START = 1;
    private static final int WORD_START = 2;
    private static final int ANYWHERE = 3;
    private static final int NO_MATCH = Integer.MAX_VALUE;

    private Search() {
    }

    /**
     * Something to find.
     *
     * @param id        its id, as {@code minecraft:stone}
     * @param name      what the player reads, in their language
     * @param namespace the mod it is from, the id's namespace
     */
    public record Entry(String id, String name, String namespace) {
    }

    /** The entries that match the text, the best matches first, in the order given among equals. */
    public static List<Entry> find(List<Entry> entries, String text) {
        String query = text.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return List.copyOf(entries);
        }
        List<Ranked> found = new ArrayList<>();
        for (Entry entry : entries) {
            int rank = rank(entry, query);
            if (rank != NO_MATCH) {
                found.add(new Ranked(entry, rank));
            }
        }
        found.sort(Comparator.comparingInt(Ranked::rank));
        return found.stream().map(Ranked::entry).toList();
    }

    /** An entry with how well it matches, ranked once rather than at every comparison of the sort. */
    private record Ranked(Entry entry, int rank) {
    }

    private static int rank(Entry entry, String query) {
        String id = entry.id().toLowerCase(Locale.ROOT);
        String name = entry.name().toLowerCase(Locale.ROOT);
        String path = id.substring(id.indexOf(':') + 1);
        if (id.equals(query) || path.equals(query)) {
            return WHOLE_ID;
        }
        if (name.startsWith(query) || path.startsWith(query)) {
            return NAME_START;
        }
        if (name.contains(" " + query) || path.contains("_" + query)) {
            return WORD_START;
        }
        return name.contains(query) || id.contains(query) ? ANYWHERE : NO_MATCH;
    }
}
