package dev.flatgen.preset.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** The game's ids, as Flatgen reads them. */
class IdsTest {
    @Test
    void readsTheNamespaceAndTheGamesForAnIdWithout() {
        assertEquals("biomesoplenty", Ids.namespace("biomesoplenty:lavender_field"));
        assertEquals("minecraft", Ids.namespace("stone"));
        assertEquals("minecraft:stone", Ids.withNamespace("stone"));
        assertEquals("create:andesite_casing", Ids.withNamespace("create:andesite_casing"));
    }
}
