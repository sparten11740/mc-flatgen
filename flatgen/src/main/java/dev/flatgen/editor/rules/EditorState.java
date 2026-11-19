package dev.flatgen.editor.rules;

import dev.flatgen.preset.rules.Preset;
import java.util.Optional;

/**
 * What the editor holds: the preset, and which of its layers is selected.
 *
 * @param preset   the preset being edited
 * @param selected the selected layer, bottom first, or -1 when there are no layers; kept within the layers
 */
public record EditorState(Preset preset, int selected) {
    public EditorState {
        int last = preset.layers().size() - 1;
        selected = last < 0 ? -1 : Math.max(0, Math.min(last, selected));
    }

    /** The selected layer's block; none when there are no layers. */
    public Optional<String> selectedBlock() {
        return selected < 0 ? Optional.empty() : Optional.of(preset.layers().get(selected).block());
    }

    /** The same preset with another layer selected. */
    public EditorState select(int layer) {
        return new EditorState(preset, layer);
    }
}
