package dev.flatgen.structures.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** What Flatgen shows of a structure set. */
class StructureSetInfoTest {
    private static final StructureSetInfo PYRAMIDS = new StructureSetInfo("minecraft:desert_pyramids",
            Set.of("minecraft:desert"));

    @Test
    void appearsOnlyInTheBiomesOfItsStructures() {
        assertEquals(true, PYRAMIDS.appearsIn("minecraft:desert"));
        assertEquals(false, PYRAMIDS.appearsIn("minecraft:plains"));
    }

    @Test
    void isNamedAfterItsId() {
        assertEquals("Desert Pyramids", PYRAMIDS.name());
        assertEquals("Towers Sky Islands", new StructureSetInfo("idas:towers/sky_islands", Set.of()).name());
    }

    @Test
    void comesFromTheNamespaceOfItsId() {
        assertEquals("minecraft", PYRAMIDS.namespace());
        assertEquals("idas", new StructureSetInfo("idas:towers", Set.of()).namespace());
    }
}
