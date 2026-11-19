package dev.flatgen.library.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Applying a saved preset in the editor, and what becomes of its missing parts. */
class LibraryEditsTest {
    private static final World OVERWORLD = new World(-64, 384);
    private static final EditorState CLASSIC = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of(
            new Layer("minecraft:bedrock", 1), new Layer("minecraft:grass_block", 1))), 0);

    @Test
    void putsThePresetInPlaceSelectingItsTopLayer() {
        EditorState applied = new ApplyPreset(TestPresets.TALL_FLAT).apply(CLASSIC, OVERWORLD);
        assertEquals(TestPresets.TALL_FLAT, applied.preset());
        assertEquals(3, applied.selected());
    }

    @Test
    void leavesOutAPresetTallerThanTheWorld() {
        Preset tower = TestPresets.TALL_FLAT.withLayers(List.of(new Layer("minecraft:stone", 400)));
        assertSame(CLASSIC, new ApplyPreset(tower).apply(CLASSIC, OVERWORLD));
    }

    @Test
    void replacesMissingBlocksWithStoneAndAMissingBiomeWithPlainsAndLeavesOutMissingSets() {
        Preset modded = TestPresets.TALL_FLAT.withLayers(List.of(new Layer("minecraft:bedrock", 1),
                new Layer("create:andesite_casing", 3))).withBiome("biomesoplenty:lavender_field")
                .withStructureSets(Optional.of(List.of("minecraft:villages", "idas:towers")));
        Preset replaced = MissingParts.replaced(modded, new MissingIds(List.of("create:andesite_casing"),
                List.of("biomesoplenty:lavender_field"), List.of("idas:towers")));
        assertEquals(List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:stone", 3)), replaced.layers());
        assertEquals("minecraft:plains", replaced.biome());
        assertEquals(Optional.of(List.of("minecraft:villages")), replaced.structureSets());
    }
}
