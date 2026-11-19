package dev.flatgen.layers.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Each layer edit on the selected layer, kept within the world. */
class LayerEditsTest {
    private static final World OVERWORLD = new World(-64, 384);
    /** The tall flat with its stone selected. */
    private static final EditorState STONE = new EditorState(TestPresets.TALL_FLAT, Dimension.OVERWORLD, 1);

    @Test
    void addsALayerOfTheSelectedBlockAboveOrBelowAndSelectsIt() {
        EditorState above = new LayerEdits.Add(true).apply(STONE, OVERWORLD);
        assertEquals(new Layer("minecraft:stone", 1), above.preset().layers().get(2));
        assertEquals(2, above.selected());
        EditorState below = new LayerEdits.Add(false).apply(STONE, OVERWORLD);
        assertEquals(new Layer("minecraft:stone", 1), below.preset().layers().get(1));
        assertEquals(1, below.selected());
        assertEquals(5, below.preset().layers().size());
    }

    @Test
    void addsStoneToAnEmptyStack() {
        EditorState empty = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of()), Dimension.OVERWORLD, -1);
        EditorState added = new LayerEdits.Add(true).apply(empty, OVERWORLD);
        assertEquals(List.of(new Layer(LayerEdits.FIRST_BLOCK, 1)), added.preset().layers());
        assertEquals(0, added.selected());
    }

    @Test
    void addsNothingToAStackAsTallAsTheWorld() {
        assertSame(STONE, new LayerEdits.Add(true).apply(STONE, new World(-64, 104)));
    }

    @Test
    void duplicatesTheSelectedLayerAboveItAsDeepAsTheWorldAllows() {
        EditorState copied = new LayerEdits.Duplicate().apply(STONE, OVERWORLD);
        assertEquals(new Layer("minecraft:stone", 100), copied.preset().layers().get(2));
        assertEquals(2, copied.selected());
        EditorState squeezed = new LayerEdits.Duplicate().apply(STONE, new World(-64, 150));
        assertEquals(new Layer("minecraft:stone", 46), squeezed.preset().layers().get(2));
    }

    @Test
    void removesTheSelectedLayerAndSelectsTheOneBelow() {
        EditorState removed = new LayerEdits.Remove().apply(STONE, OVERWORLD);
        assertEquals(3, removed.preset().layers().size());
        assertEquals(0, removed.selected());
        EditorState bedrockGone = new LayerEdits.Remove().apply(STONE.select(0), OVERWORLD);
        assertEquals(0, bedrockGone.selected());
        assertEquals("minecraft:stone", bedrockGone.preset().layers().get(0).block());
    }

    @Test
    void movesTheSelectedLayerAndKeepsItSelected() {
        EditorState moved = new LayerEdits.Move(3).apply(STONE, OVERWORLD);
        assertEquals("minecraft:stone", moved.preset().layers().get(3).block());
        assertEquals("minecraft:grass_block", moved.preset().layers().get(2).block());
        assertEquals(3, moved.selected());
        assertSame(STONE, new LayerEdits.Move(4).apply(STONE, OVERWORLD));
        assertSame(STONE, new LayerEdits.Move(1).apply(STONE, OVERWORLD));
    }

    @Test
    void setsAHeightOfAtLeastOneAndAtMostWhatTheWorldAllows() {
        assertEquals(50, height(new LayerEdits.SetHeight(50).apply(STONE, OVERWORLD)));
        assertEquals(1, height(new LayerEdits.SetHeight(0).apply(STONE, OVERWORLD)));
        assertEquals(380, height(new LayerEdits.SetHeight(1000).apply(STONE, OVERWORLD)));
        assertEquals(380, LayerEdits.room(STONE, OVERWORLD));
    }

    @Test
    void changesTheHeightByAnAmount() {
        assertEquals(110, height(new LayerEdits.ChangeHeight(10).apply(STONE, OVERWORLD)));
        assertEquals(1, height(new LayerEdits.ChangeHeight(-500).apply(STONE, OVERWORLD)));
    }

    @Test
    void putsTheSurfaceAtAYByTheSelectedLayer() {
        EditorState raised = new LayerEdits.SurfaceAt(64).apply(STONE, OVERWORLD);
        assertEquals(124, height(raised));
        assertEquals(64, OVERWORLD.floor() + raised.preset().height());
        assertEquals(1, height(new LayerEdits.SurfaceAt(-100).apply(STONE, OVERWORLD)));
    }

    @Test
    void replacesTheLayersAndBiomeAndSelectsTheTopLayer() {
        List<Layer> sandy = List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:sand", 5));
        EditorState pasted = new LayerEdits.Replace(sandy, "minecraft:desert").apply(STONE, OVERWORLD);
        assertEquals(sandy, pasted.preset().layers());
        assertEquals("minecraft:desert", pasted.preset().biome());
        assertEquals(1, pasted.selected());
        assertSame(STONE, new LayerEdits.Replace(List.of(new Layer("minecraft:stone", 400)), "minecraft:plains")
                .apply(STONE, OVERWORLD));
    }

    @Test
    void leavesAnEmptyStackAsItIs() {
        EditorState empty = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of()), Dimension.OVERWORLD, -1);
        assertSame(empty, new LayerEdits.Remove().apply(empty, OVERWORLD));
        assertSame(empty, new LayerEdits.SetHeight(3).apply(empty, OVERWORLD));
        assertSame(empty, new LayerEdits.Duplicate().apply(empty, OVERWORLD));
    }

    private static int height(EditorState state) {
        Preset preset = state.preset();
        return preset.layers().get(state.selected()).height();
    }
}
