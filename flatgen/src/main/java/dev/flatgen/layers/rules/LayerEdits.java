package dev.flatgen.layers.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;

/**
 * The edits of the layer stack, each on the selected layer, each keeping the stack within the world: a change that
 * would make it taller than the world goes as far as the world allows, or is not made.
 */
public final class LayerEdits {
    /** The block of a layer added to an empty stack, which has no selected layer to copy. */
    static final String FIRST_BLOCK = "minecraft:stone";

    private LayerEdits() {
    }

    /**
     * Adds a layer one block deep above or below the selected one, of its block, and selects it.
     *
     * @param above whether it goes above the selected layer rather than below
     */
    public record Add(boolean above) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            Preset preset = state.preset();
            if (preset.height() >= world.height()) {
                return state;
            }
            String block = state.selected() < 0 ? FIRST_BLOCK : preset.layers().get(state.selected()).block();
            int at = state.selected() < 0 ? 0 : state.selected() + (above ? 1 : 0);
            return inserted(state, at, new Layer(block, 1));
        }
    }

    /** Adds a copy of the selected layer above it, as deep as the world allows, and selects the copy. */
    public record Duplicate() implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            int room = world.height() - state.preset().height();
            if (state.selected() < 0 || room < 1) {
                return state;
            }
            Layer layer = state.preset().layers().get(state.selected());
            return inserted(state, state.selected() + 1, new Layer(layer.block(), Math.min(layer.height(), room)));
        }
    }

    /** Removes the selected layer, selecting the one that was below it. */
    public record Remove() implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.selected() < 0) {
                return state;
            }
            List<Layer> layers = new ArrayList<>(state.preset().layers());
            layers.remove(state.selected());
            return new EditorState(state.preset().withLayers(layers), state.selected() - 1);
        }
    }

    /**
     * Moves the selected layer to a new place in the stack, and keeps it selected.
     *
     * @param to the place it ends up at, bottom first
     */
    public record Move(int to) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            int last = state.preset().layers().size() - 1;
            if (state.selected() < 0 || to < 0 || to > last || to == state.selected()) {
                return state;
            }
            List<Layer> layers = new ArrayList<>(state.preset().layers());
            layers.add(to, layers.remove(state.selected()));
            return new EditorState(state.preset().withLayers(layers), to);
        }
    }

    /**
     * Makes the selected layer this many blocks deep, at least one and at most what the world allows.
     *
     * @param height the depth asked for, in blocks
     */
    public record SetHeight(int height) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.selected() < 0) {
                return state;
            }
            int clamped = Math.max(1, Math.min(room(state, world), height));
            List<Layer> layers = new ArrayList<>(state.preset().layers());
            layers.set(state.selected(), new Layer(layers.get(state.selected()).block(), clamped));
            return new EditorState(state.preset().withLayers(layers), state.selected());
        }
    }

    /**
     * Makes the selected layer deeper or shallower by some blocks, within what {@link SetHeight} allows.
     *
     * @param blocks how many blocks deeper, negative for shallower
     */
    public record ChangeHeight(int blocks) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.selected() < 0) {
                return state;
            }
            return new SetHeight(state.preset().layers().get(state.selected()).height() + blocks).apply(state, world);
        }
    }

    /**
     * Makes the selected layer as deep as it takes for the surface, where mobs stand, to be at this Y, as far as one
     * layer can.
     *
     * @param y the surface's Y asked for
     */
    public record SurfaceAt(int y) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.selected() < 0) {
                return state;
            }
            int others = state.preset().height() - state.preset().layers().get(state.selected()).height();
            return new SetHeight(y - world.floor() - others).apply(state, world);
        }
    }

    /** How deep the selected layer may be: the world's height less the layers around it. */
    public static int room(EditorState state, World world) {
        return world.height() - state.preset().height() + state.preset().layers().get(state.selected()).height();
    }

    private static EditorState inserted(EditorState state, int at, Layer layer) {
        List<Layer> layers = new ArrayList<>(state.preset().layers());
        layers.add(at, layer);
        return new EditorState(state.preset().withLayers(layers), at);
    }
}
