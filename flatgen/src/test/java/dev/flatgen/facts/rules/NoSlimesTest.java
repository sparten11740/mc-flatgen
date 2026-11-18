package dev.flatgen.facts.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Raising the surface out of slime chunks' reach. */
class NoSlimesTest {
    private static final World OVERWORLD = new World(-64, 384);

    @Test
    void addsTheBlocksToTheThickestLayerBetweenTheBottomAndTheTop() {
        EditorState classic = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of(
                new Layer("minecraft:bedrock", 1), new Layer("minecraft:stone", 3), new Layer("minecraft:dirt", 2),
                new Layer("minecraft:grass_block", 1))), 3);
        EditorState raised = new NoSlimes().apply(classic, OVERWORLD);
        assertEquals(new Layer("minecraft:stone", 3 + 97), raised.preset().layers().get(1));
        assertEquals(40, OVERWORLD.floor() + raised.preset().height());
        assertEquals(3, raised.selected());
    }

    @Test
    void keepsBedrockAFloorInTheClassicFlatWorld() {
        EditorState classic = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of(
                new Layer("minecraft:bedrock", 1), new Layer("minecraft:dirt", 2),
                new Layer("minecraft:grass_block", 1))), 2);
        assertEquals(List.of(new Layer("minecraft:bedrock", 1), new Layer("minecraft:dirt", 102),
                new Layer("minecraft:grass_block", 1)), new NoSlimes().apply(classic, OVERWORLD).preset().layers());
    }

    @Test
    void addsToTheBottomLayerOfAShortStack() {
        EditorState two = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of(
                new Layer("minecraft:bedrock", 1), new Layer("minecraft:grass_block", 1))), 1);
        assertEquals(new Layer("minecraft:bedrock", 103), new NoSlimes().apply(two, OVERWORLD).preset().layers()
                .get(0));
    }

    @Test
    void leavesAHighEnoughOrEmptyStackAsItIs() {
        EditorState tall = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of(
                new Layer("minecraft:stone", 200))), 0);
        assertSame(tall, new NoSlimes().apply(tall, OVERWORLD));
        EditorState empty = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of()), -1);
        assertSame(empty, new NoSlimes().apply(empty, OVERWORLD));
    }
}
