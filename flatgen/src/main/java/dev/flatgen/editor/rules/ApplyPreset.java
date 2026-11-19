package dev.flatgen.editor.rules;

import dev.flatgen.preset.rules.Preset;

/**
 * Puts a whole preset in the editor in place of what it holds, a saved one or one a template made, the same dimension
 * edited with its top layer selected, as one step to undo; a preset whose edited stack is taller than the world is left
 * out, the editor as it was.
 *
 * @param preset the preset
 */
public record ApplyPreset(Preset preset) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        EditorState next = state.using(preset);
        return next.fits(world) ? next : state;
    }
}
