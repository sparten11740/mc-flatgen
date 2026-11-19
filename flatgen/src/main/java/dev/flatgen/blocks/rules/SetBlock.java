package dev.flatgen.blocks.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes the selected layer of another block, keeping its depth.
 *
 * @param block the block's id
 */
public record SetBlock(String block) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        if (state.selected() < 0) {
            return state;
        }
        List<Layer> layers = new ArrayList<>(state.layers());
        layers.set(state.selected(), new Layer(block, layers.get(state.selected()).height()));
        return state.withLayers(layers, state.selected());
    }
}
