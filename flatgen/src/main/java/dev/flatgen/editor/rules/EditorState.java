package dev.flatgen.editor.rules;

import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.Stack;
import java.util.List;
import java.util.Optional;

/**
 * What the editor holds: the preset, which dimension's stack is edited (F22), and which of its layers is selected.
 * Edits of layers and biome go through it, so they change the edited dimension's stack whichever it is.
 *
 * @param preset   the preset being edited
 * @param edited   the dimension whose stack the column and the layer controls show
 * @param selected the selected layer of the edited stack, bottom first, or -1 when it has no layers; kept within them
 */
public record EditorState(Preset preset, Dimension edited, int selected) {
    /** A selection above every layer, which the record keeps within them: the top layer. */
    private static final int TOP = Integer.MAX_VALUE;

    public EditorState {
        int last = preset.stack(edited).map(stack -> stack.layers().size()).orElse(0) - 1;
        selected = last < 0 ? -1 : Math.max(0, Math.min(last, selected));
    }

    /** A preset with its overworld edited and its top layer selected, as the editor opens on it. */
    public static EditorState of(Preset preset) {
        return new EditorState(preset, Dimension.OVERWORLD, TOP);
    }

    /** The edited dimension's stack; none for a Nether or End the preset leaves as the game makes it. */
    public Optional<Stack> stack() {
        return preset.stack(edited);
    }

    /** The edited stack's layers, bottom first; none without a stack. */
    public List<Layer> layers() {
        return stack().map(Stack::layers).orElse(List.of());
    }

    /** The edited stack's biome, or the dimension's own without a stack. */
    public String biome() {
        return stack().map(Stack::biome).orElse(edited.defaultBiome());
    }

    /** How tall the edited stack is, in blocks. */
    public int height() {
        return stack().map(Stack::height).orElse(0);
    }

    /** Whether the edited stack fits its world: no taller than the world's height. */
    public boolean fits(World world) {
        return height() <= world.height();
    }

    /** The selected layer's block; none when there are no layers. */
    public Optional<String> selectedBlock() {
        return selected < 0 ? Optional.empty() : Optional.of(layers().get(selected).block());
    }

    /** The same preset with another layer selected. */
    public EditorState select(int layer) {
        return new EditorState(preset, edited, layer);
    }

    /** The same preset with another dimension's stack edited, its top layer selected. */
    public EditorState choose(Dimension dimension) {
        return new EditorState(preset, dimension, TOP);
    }

    /** The edited stack with other layers and this layer selected; as it is without a stack. */
    public EditorState withLayers(List<Layer> layers, int newSelected) {
        return stack().map(own -> withEditedStack(new Stack(layers, own.biome()), newSelected)).orElse(this);
    }

    /** The edited stack with another biome; as it is without a stack. */
    public EditorState withBiome(String biome) {
        return stack().map(own -> withEditedStack(new Stack(own.layers(), biome), selected)).orElse(this);
    }

    /** The edited dimension given this stack, or none to leave it as the game makes it, its top layer selected. */
    public EditorState withStack(Optional<Stack> stack) {
        if (edited == Dimension.OVERWORLD && stack.isEmpty()) {
            return this;
        }
        return new EditorState(preset.withStack(edited, stack), edited, TOP);
    }

    /** Another preset, as a change of the whole preset makes it, the same dimension edited and layer selected. */
    public EditorState withPreset(Preset changed) {
        return new EditorState(changed, edited, selected);
    }

    /** A whole other preset put in, a saved one or a template's, the same dimension edited and its top selected. */
    public EditorState using(Preset other) {
        return new EditorState(other, edited, TOP);
    }

    private EditorState withEditedStack(Stack stack, int newSelected) {
        return new EditorState(preset.withStack(edited, Optional.of(stack)), edited, newSelected);
    }
}
