package dev.flatgen.editor.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.Stack;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Putting a whole preset in the editor. */
class ApplyPresetTest {
    private static final World OVERWORLD = new World(-64, 384);
    private static final EditorState CLASSIC = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of(
            new Layer("minecraft:bedrock", 1), new Layer("minecraft:grass_block", 1))), Dimension.OVERWORLD, 0);

    @Test
    void putsThePresetInPlaceSelectingItsTopLayer() {
        EditorState applied = new ApplyPreset(TestPresets.TALL_FLAT).apply(CLASSIC, OVERWORLD);
        assertEquals(TestPresets.TALL_FLAT, applied.preset());
        assertEquals(3, applied.selected());
    }

    @Test
    void takesTheSavedPresetsNetherAndEndAndKeepsTheEditedDimension() {
        Stack nether = new Stack(List.of(new Layer("minecraft:netherrack", 4)), "minecraft:crimson_forest");
        Preset saved = TestPresets.TALL_FLAT.withStack(Dimension.NETHER, Optional.of(nether));
        EditorState applied = new ApplyPreset(saved).apply(CLASSIC.choose(Dimension.NETHER), new World(0, 256));
        assertEquals(saved, applied.preset());
        assertEquals(Dimension.NETHER, applied.edited());
        assertEquals(0, applied.selected());
    }

    @Test
    void leavesOutAPresetTallerThanTheWorld() {
        Preset tower = TestPresets.TALL_FLAT.withLayers(List.of(new Layer("minecraft:stone", 400)));
        assertSame(CLASSIC, new ApplyPreset(tower).apply(CLASSIC, OVERWORLD));
    }
}
