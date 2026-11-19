package dev.flatgen.structures.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import java.util.List;
import java.util.Optional;

/**
 * Sets which structure sets the preset generates, as {@link StructureChoice} works them out; one step to undo.
 *
 * @param sets the chosen list; none for what the biome allows
 */
public record SetStructures(Optional<List<String>> sets) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        return state.withPreset(state.preset().withStructureSets(sets));
    }
}
