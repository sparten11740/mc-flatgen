package dev.flatgen.dimensions.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Stack;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The Nether's and the End's first stacks, and giving a dimension a stack or taking it back (F22). */
class DimensionRulesTest {
    /** The Nether and the End as the game makes them: floor at Y 0, 256 blocks tall. */
    private static final World NETHER_OR_END = new World(0, 256);

    @Test
    void theNethersFirstStackHasItsSurfaceWhereTheLavaSeaLies() {
        Stack nether = NewStacks.of(Dimension.NETHER);
        assertEquals(new Layer("minecraft:bedrock", 1), nether.layers().get(0));
        assertEquals(32, nether.height());
        assertEquals("minecraft:nether_wastes", nether.biome());
    }

    @Test
    void theEndsFirstStackEndsInTheLayerTheArrivalPlatformIsLaidIn() {
        Stack end = NewStacks.of(Dimension.END);
        assertEquals(48, end.height() - 1);
        assertEquals("minecraft:end_stone", end.layers().get(end.layers().size() - 1).block());
        assertEquals("minecraft:the_end", end.biome());
    }

    @Test
    void givesTheEditedDimensionAStackSelectsItsTopAndTakesItBack() {
        EditorState end = EditorState.of(TestPresets.TALL_FLAT).choose(Dimension.END);
        EditorState flat = new SetStack(Optional.of(NewStacks.of(Dimension.END))).apply(end, NETHER_OR_END);
        assertEquals(Optional.of(NewStacks.of(Dimension.END)), flat.preset().stack(Dimension.END));
        assertEquals(1, flat.selected());
        EditorState back = new SetStack(Optional.empty()).apply(flat, NETHER_OR_END);
        assertEquals(Optional.empty(), back.preset().stack(Dimension.END));
        assertEquals(TestPresets.TALL_FLAT, back.preset());
    }

    @Test
    void givesNoStackTallerThanTheDimension() {
        EditorState nether = EditorState.of(TestPresets.TALL_FLAT).choose(Dimension.NETHER);
        Stack tall = new Stack(List.of(new Layer("minecraft:netherrack", 257)), "minecraft:nether_wastes");
        assertEquals(nether, new SetStack(Optional.of(tall)).apply(nether, NETHER_OR_END));
    }
}
