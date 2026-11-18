package dev.flatgen.blocks.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The blocks picked lately and starred. */
class BlockMemoryTest {
    @Test
    void putsAPickedBlockFirstOnceOnly() {
        BlockMemory memory = BlockMemory.EMPTY.used("minecraft:stone").used("minecraft:dirt").used("minecraft:stone");
        assertEquals(List.of("minecraft:stone", "minecraft:dirt"), memory.recent());
    }

    @Test
    void keepsOnlyTheLatestSixteen() {
        BlockMemory memory = BlockMemory.EMPTY;
        for (int index = 0; index < 20; index++) {
            memory = memory.used("mod:block_" + index);
        }
        assertEquals(BlockMemory.MOST_RECENT, memory.recent().size());
        assertEquals("mod:block_19", memory.recent().get(0));
    }

    @Test
    void starsAndUnstars() {
        BlockMemory starred = BlockMemory.EMPTY.toggleStar("minecraft:glass").toggleStar("minecraft:stone");
        assertEquals(List.of("minecraft:glass", "minecraft:stone"), starred.starred());
        assertEquals(List.of("minecraft:stone"), starred.toggleStar("minecraft:glass").starred());
    }

    @Test
    void readsWhatItWritesAndLeavesOutWhatIsNotAnId() {
        BlockMemory memory = BlockMemory.EMPTY.used("minecraft:stone").toggleStar("minecraft:glass");
        assertEquals(memory, BlockMemory.fromJson(memory.toJson()));
        BlockMemory read = BlockMemory.fromJson("{\"recent\": [\"minecraft:dirt\", 3], \"starred\": \"no\"}");
        assertEquals(List.of("minecraft:dirt"), read.recent());
        assertEquals(new ArrayList<>(), read.starred());
    }
}
