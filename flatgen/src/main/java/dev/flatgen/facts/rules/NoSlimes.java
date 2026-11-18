package dev.flatgen.facts.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import java.util.ArrayList;
import java.util.List;

/**
 * Raises the stack so its surface is where slimes no longer spawn in slime chunks,
 * {@link WorldFacts#SLIME_CHUNK_MAX_Y}, adding the blocks to the thickest layer between the bottom and the top one (the
 * bottom one in a stack of two or fewer), so the floor and the surface the player built stay as they are; one step to
 * undo.
 */
public record NoSlimes() implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        List<Layer> layers = new ArrayList<>(state.preset().layers());
        int surface = world.floor() + state.preset().height();
        int room = world.height() - state.preset().height();
        int added = Math.min(room, WorldFacts.SLIME_CHUNK_MAX_Y - surface);
        if (layers.isEmpty() || added <= 0) {
            return state;
        }
        int thickest = thickestInBetween(layers);
        layers.set(thickest, new Layer(layers.get(thickest).block(), layers.get(thickest).height() + added));
        return new EditorState(state.preset().withLayers(layers), state.selected());
    }

    /** The thickest layer between the bottom and the top one, the lowest of equals; else the bottom one. */
    private static int thickestInBetween(List<Layer> layers) {
        int thickest = layers.size() > 2 ? 1 : 0;
        for (int index = 2; index < layers.size() - 1; index++) {
            if (layers.get(index).height() > layers.get(thickest).height()) {
                thickest = index;
            }
        }
        return thickest;
    }
}
