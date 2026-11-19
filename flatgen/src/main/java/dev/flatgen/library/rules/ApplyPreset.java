package dev.flatgen.library.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Preset;

/**
 * Puts a saved preset in the editor in place of what it holds, its top layer selected, as one step to undo; a preset
 * taller than the world is left out, the editor as it was.
 *
 * @param preset the preset
 */
public record ApplyPreset(Preset preset) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        return preset.fitsWorld(world.height()) ? new EditorState(preset, preset.layers().size() - 1) : state;
    }
}
