package dev.flatgen.editor.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Stack;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** What the editor holds: its selection kept within the edited stack, and edits of whichever stack is edited. */
class EditorStateTest {
    @Test
    void keepsTheSelectionWithinTheLayersAndGivesItsBlock() {
        EditorState top = new EditorState(TestPresets.TALL_FLAT, Dimension.OVERWORLD, 99);
        assertEquals(3, top.selected());
        assertEquals(Optional.of("minecraft:grass_block"), top.selectedBlock());
        assertEquals(Optional.of("minecraft:bedrock"), top.select(-5).selectedBlock());
    }

    @Test
    void editsTheChosenDimensionsStackAndLeavesTheOverworldAlone() {
        Stack nether = new Stack(List.of(new Layer("minecraft:netherrack", 3)), "minecraft:nether_wastes");
        EditorState state = EditorState.of(TestPresets.TALL_FLAT.withStack(Dimension.NETHER, Optional.of(nether)))
                .choose(Dimension.NETHER);
        assertEquals(0, state.selected());
        EditorState edited = state.withLayers(List.of(new Layer("minecraft:netherrack", 5)), 0)
                .withBiome("minecraft:basalt_deltas");
        assertEquals(Optional.of(new Stack(List.of(new Layer("minecraft:netherrack", 5)), "minecraft:basalt_deltas")),
                edited.preset().stack(Dimension.NETHER));
        assertEquals(TestPresets.TALL_FLAT.layers(), edited.preset().layers());
        assertEquals(TestPresets.TALL_FLAT.biome(), edited.preset().biome());
    }

    @Test
    void aDimensionWithoutAStackHasNoLayersAndItsOwnBiomeAndTakesNoEdits() {
        EditorState end = EditorState.of(TestPresets.TALL_FLAT).choose(Dimension.END);
        assertEquals(List.of(), end.layers());
        assertEquals(-1, end.selected());
        assertEquals(0, end.height());
        assertEquals("minecraft:the_end", end.biome());
        assertEquals(end, end.withLayers(List.of(new Layer("minecraft:end_stone", 1)), 0));
        assertEquals(end, end.withBiome("minecraft:plains"));
    }

    @Test
    void theOverworldAlwaysKeepsItsStack() {
        EditorState overworld = EditorState.of(TestPresets.TALL_FLAT);
        assertEquals(overworld, overworld.withStack(Optional.empty()));
    }

    @Test
    void selectsNothingAndGivesNoBlockWithoutLayers() {
        EditorState empty = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of()), Dimension.OVERWORLD, 2);
        assertEquals(-1, empty.selected());
        assertEquals(Optional.empty(), empty.selectedBlock());
    }
}
