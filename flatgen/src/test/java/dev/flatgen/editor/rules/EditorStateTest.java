package dev.flatgen.editor.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** What the editor holds: its selection kept within the layers, and the selected layer's block. */
class EditorStateTest {
    @Test
    void keepsTheSelectionWithinTheLayersAndGivesItsBlock() {
        EditorState top = new EditorState(TestPresets.TALL_FLAT, 99);
        assertEquals(3, top.selected());
        assertEquals(Optional.of("minecraft:grass_block"), top.selectedBlock());
        assertEquals(Optional.of("minecraft:bedrock"), top.select(-5).selectedBlock());
    }

    @Test
    void selectsNothingAndGivesNoBlockWithoutLayers() {
        EditorState empty = new EditorState(TestPresets.TALL_FLAT.withLayers(List.of()), 2);
        assertEquals(-1, empty.selected());
        assertEquals(Optional.empty(), empty.selectedBlock());
    }
}
