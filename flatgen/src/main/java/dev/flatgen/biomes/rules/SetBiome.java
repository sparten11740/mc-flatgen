package dev.flatgen.biomes.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;

/**
 * Gives the preset another biome, one step to undo.
 *
 * @param biome the biome's id
 */
public record SetBiome(String biome) implements Edit {
    @Override
    public EditorState apply(EditorState state, World world) {
        return state.withBiome(biome);
    }
}
