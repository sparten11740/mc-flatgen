package dev.flatgen.search.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** What a picker shows, in order. */
class PickSectionsTest {
    private static final Search.Entry STONE = new Search.Entry("minecraft:stone", "Stone", "minecraft");
    private static final Search.Entry GLASS = new Search.Entry("minecraft:glass", "Glass", "minecraft");
    private static final Search.Entry CASING = new Search.Entry("create:andesite_casing", "Andesite Casing", "create");
    private static final List<Search.Entry> ALL = List.of(STONE, GLASS, CASING);
    private static final Picks PICKS = Picks.EMPTY.used("minecraft:glass").used("gone:block")
            .toggleStar("create:andesite_casing");

    @Test
    void showsTheStarredTheRecentAndThenAll() {
        List<PickSections.Section> sections = PickSections.of(ALL, ALL, "", PICKS);
        assertEquals(List.of(PickSections.Kind.STARRED, PickSections.Kind.RECENT, PickSections.Kind.ALL),
                sections.stream().map(PickSections.Section::kind).toList());
        assertEquals(List.of(CASING), sections.get(0).entries());
        assertEquals(List.of(GLASS), sections.get(1).entries());
    }

    @Test
    void keepsStarsWhateverEntriesAreShownAndLeavesOutEmptySections() {
        List<PickSections.Section> sections = PickSections.of(ALL, List.of(STONE, GLASS), "", Picks.EMPTY
                .toggleStar("create:andesite_casing"));
        assertEquals(List.of(PickSections.Kind.STARRED, PickSections.Kind.ALL),
                sections.stream().map(PickSections.Section::kind).toList());
        assertEquals(List.of(CASING), sections.get(0).entries());
    }

    @Test
    void showsOnlyWhatASearchFoundAmongTheEntriesShown() {
        assertEquals(List.of(new PickSections.Section(PickSections.Kind.FOUND, List.of(GLASS))),
                PickSections.of(ALL, List.of(STONE, GLASS), "gla", PICKS));
    }
}
