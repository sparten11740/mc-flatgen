package dev.flatgen.search.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** What a player picked lately and starred. */
class PicksTest {
    @Test
    void putsAPickFirstOnceOnly() {
        Picks picks = Picks.EMPTY.used("minecraft:stone").used("minecraft:dirt").used("minecraft:stone");
        assertEquals(List.of("minecraft:stone", "minecraft:dirt"), picks.recent());
    }

    @Test
    void keepsOnlyTheLatestSixteen() {
        Picks picks = Picks.EMPTY;
        for (int index = 0; index < 20; index++) {
            picks = picks.used("mod:block_" + index);
        }
        assertEquals(Picks.MOST_RECENT, picks.recent().size());
        assertEquals("mod:block_19", picks.recent().get(0));
    }

    @Test
    void starsAndUnstars() {
        Picks starred = Picks.EMPTY.toggleStar("minecraft:glass").toggleStar("minecraft:stone");
        assertEquals(List.of("minecraft:glass", "minecraft:stone"), starred.starred());
        assertEquals(List.of("minecraft:stone"), starred.toggleStar("minecraft:glass").starred());
    }

    @Test
    void readsWhatItWritesAndLeavesOutWhatIsNotText() {
        Picks picks = Picks.EMPTY.used("minecraft:stone").toggleStar("minecraft:glass");
        assertEquals(picks, Picks.fromJson(picks.toJson()));
        Picks read = Picks.fromJson("{\"recent\": [\"minecraft:dirt\", 3], \"starred\": \"no\"}");
        assertEquals(List.of("minecraft:dirt"), read.recent());
        assertEquals(List.of(), read.starred());
    }
}
