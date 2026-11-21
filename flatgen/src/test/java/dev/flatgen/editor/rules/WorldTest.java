package dev.flatgen.editor.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** The world a stack stands in. */
class WorldTest {
    @Test
    void itsTopIsTheYOfItsHighestBlock() {
        assertEquals(319, new World(-64, 384).top());
        assertEquals(255, new World(0, 256).top());
    }
}
