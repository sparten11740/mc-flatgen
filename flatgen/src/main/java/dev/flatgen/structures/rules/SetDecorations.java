package dev.flatgen.structures.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;

/**
 * Turns the biome's decorations (trees, flowers, ores) and the lakes on or off; one step to undo.
 *
 * @param decorations whether the biome's features generate
 * @param lakes       whether lakes generate
 */
public record SetDecorations(boolean decorations, boolean lakes) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        return new EditorState(state.preset().withDecorations(decorations, lakes), state.selected());
    }
}
