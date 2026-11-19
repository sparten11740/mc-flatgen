package dev.flatgen.blocks.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Choosing a layer's block, and the hints for blocks that make poor layers. */
class BlockRulesTest {
    private static final LayerHints.Traits SAND = new LayerHints.Traits(true, true, false, true);
    private static final LayerHints.Traits STONE = new LayerHints.Traits(false, true, false, true);
    private static final LayerHints.Traits WATER = new LayerHints.Traits(false, true, false, false);
    private static final LayerHints.Traits TORCH = new LayerHints.Traits(false, false, false, false);
    private static final LayerHints.Traits CHEST = new LayerHints.Traits(false, false, true, true);

    @Test
    void setsTheSelectedLayersBlockAndKeepsItsDepth() {
        EditorState state = new EditorState(TestPresets.TALL_FLAT, Dimension.OVERWORLD, 1);
        EditorState glass = new SetBlock("minecraft:glass").apply(state, new World(-64, 384));
        assertEquals(new Layer("minecraft:glass", 100), glass.preset().layers().get(1));
        assertEquals(1, glass.selected());
    }

    @Test
    void warnsOfAFallingBlockOverAirAFluidOrTheVoidOnly() {
        assertEquals(List.of(LayerHints.Hint.FALLS), LayerHints.of(SAND, Optional.empty()));
        assertEquals(List.of(LayerHints.Hint.FALLS), LayerHints.of(SAND, Optional.of(WATER)));
        assertEquals(List.of(), LayerHints.of(SAND, Optional.of(STONE)));
    }

    @Test
    void warnsOfBlocksThatAreNotWholeOrKeepData() {
        assertEquals(List.of(LayerHints.Hint.NOT_WHOLE), LayerHints.of(TORCH, Optional.of(STONE)));
        assertEquals(List.of(LayerHints.Hint.NOT_WHOLE, LayerHints.Hint.KEEPS_DATA),
                LayerHints.of(CHEST, Optional.of(STONE)));
        assertEquals(List.of(), LayerHints.of(STONE, Optional.empty()));
    }
}
