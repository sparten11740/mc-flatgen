package dev.flatgen.options.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import java.util.Map;

/**
 * Sets which world options the preset has on, as {@link OptionChoices} works them out, as one step to undo.
 *
 * @param options the options set otherwise than their default, by id
 */
public record SetOptions(Map<String, Boolean> options) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        return state.withPreset(state.preset().withOptions(options));
    }
}
