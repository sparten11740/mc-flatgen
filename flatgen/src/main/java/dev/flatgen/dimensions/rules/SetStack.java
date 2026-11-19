package dev.flatgen.dimensions.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Stack;
import java.util.Optional;

/**
 * Gives the edited Nether or End a stack of its own, or none to leave it as the game makes it (F22); a stack taller
 * than the dimension is not given, and the overworld always keeps its own.
 *
 * @param stack the stack, or none
 */
public record SetStack(Optional<Stack> stack) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        EditorState next = state.withStack(stack);
        return next.fits(world) ? next : state;
    }
}
