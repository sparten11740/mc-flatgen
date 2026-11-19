package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.TestPresets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** What flatgen:editor reports of the editor's preset. */
class EditorFieldsTest {
    @Test
    void reportsTheLayersAsTheGamesCodeAndWhereMobsStand() {
        Map<String, Object> fields = EditorFields.of(new EditorState(TestPresets.TALL_FLAT, Dimension.OVERWORLD, 3),
                -64);
        assertEquals(true, fields.get("open"));
        assertEquals("minecraft:bedrock,100*minecraft:stone,2*minecraft:dirt,minecraft:grass_block;minecraft:plains",
                fields.get("code"));
        assertEquals(4, fields.get("layers"));
        assertEquals(104, fields.get("height"));
        assertEquals(40, fields.get("surface_y"));
        assertEquals("minecraft:plains", fields.get("biome"));
        assertEquals("l3", fields.get("selected"));
        assertEquals("overworld", fields.get("dimension"));
        assertEquals(true, fields.get("flat"));
    }

    @Test
    void reportsTheEditedNetherOrEndAndWhetherItIsFlat() {
        Map<String, Object> fields = EditorFields.of(new EditorState(TestPresets.TALL_FLAT, Dimension.NETHER, 0), 0);
        assertEquals("nether", fields.get("dimension"));
        assertEquals(false, fields.get("flat"));
        assertEquals(0, fields.get("layers"));
        assertEquals("minecraft:nether_wastes", fields.get("biome"));
        assertEquals("none", fields.get("selected"));
    }

    @Test
    void saysItsFieldsInAFixedOrder() {
        assertEquals(List.of("open", "code", "dimension", "flat", "layers", "height", "surface_y", "biome", "selected"),
                new ArrayList<>(EditorFields.of(EditorState.of(TestPresets.TALL_FLAT), -64).keySet()));
    }
}
