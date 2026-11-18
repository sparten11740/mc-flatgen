package dev.flatgen.compat.blockpilot.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.TestPresets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** What flatgen:editor reports of the editor's preset. */
class EditorFieldsTest {
    @Test
    void reportsTheLayersAsTheGamesCodeAndWhereMobsStand() {
        Map<String, Object> fields = EditorFields.of(TestPresets.TALL_FLAT, -64);
        assertEquals(true, fields.get("open"));
        assertEquals("minecraft:bedrock,100*minecraft:stone,2*minecraft:dirt,minecraft:grass_block;minecraft:plains",
                fields.get("code"));
        assertEquals(4, fields.get("layers"));
        assertEquals(104, fields.get("height"));
        assertEquals(40, fields.get("surface_y"));
        assertEquals("minecraft:plains", fields.get("biome"));
    }

    @Test
    void saysItsFieldsInAFixedOrder() {
        assertEquals(List.of("open", "code", "layers", "height", "surface_y", "biome"),
                new ArrayList<>(EditorFields.of(TestPresets.TALL_FLAT, -64).keySet()));
    }
}
