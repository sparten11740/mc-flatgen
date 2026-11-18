package dev.flatgen.search.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * What a picker shows, in order: what a search found, or else the starred, the recent and then all its entries, each
 * section left out when empty.
 */
public final class PickSections {
    private PickSections() {
    }

    /** What a section is. */
    public enum Kind {
        /** What a search found among the entries shown. */
        FOUND,
        /** The starred entries. */
        STARRED,
        /** The entries picked lately. */
        RECENT,
        /** Every entry shown. */
        ALL
    }

    /**
     * A part of a picker under its heading.
     *
     * @param kind    what it is
     * @param entries its entries, in order
     */
    public record Section(Kind kind, List<Search.Entry> entries) {
    }

    /**
     * The sections for a search over the entries shown (those of the mod chosen, say); the starred and recent ones
     * come from all entries, so a star shows whatever the mod, and ids of entries the game no longer has are left out.
     */
    public static List<Section> of(List<Search.Entry> all, List<Search.Entry> shown, String query, Picks picks) {
        if (!query.isBlank()) {
            return List.of(new Section(Kind.FOUND, Search.find(shown, query)));
        }
        Map<String, Search.Entry> byId = all.stream()
                .collect(Collectors.toMap(Search.Entry::id, Function.identity(), (first, second) -> first));
        List<Section> sections = new ArrayList<>();
        addUnlessEmpty(sections, Kind.STARRED, picks.starred(), byId);
        addUnlessEmpty(sections, Kind.RECENT, picks.recent(), byId);
        sections.add(new Section(Kind.ALL, shown));
        return sections;
    }

    private static void addUnlessEmpty(List<Section> sections, Kind kind, List<String> ids,
            Map<String, Search.Entry> byId) {
        List<Search.Entry> entries = ids.stream().map(byId::get).filter(Objects::nonNull).toList();
        if (!entries.isEmpty()) {
            sections.add(new Section(kind, entries));
        }
    }
}
