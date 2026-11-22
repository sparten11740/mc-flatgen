package dev.flatgen.layers.rules;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import java.util.ArrayList;
import java.util.List;

/**
 * The edits of the layer stack, each on the selected layer, each keeping the stack within the world: a change that
 * would make it taller than the world goes as far as the world allows, or is not made.
 */
public final class LayerEdits {
    private LayerEdits() {
    }

    /**
     * Adds a layer one block deep of a block at a place in the stack, and selects it.
     *
     * @param block the block it is of, as the block picker picked it
     * @param at    where it goes, counted from the bottom: 0 below every layer, the number of layers on top
     */
    public record Insert(String block, int at) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.height() >= world.height()) {
                return state;
            }
            return inserted(state, Math.max(0, Math.min(at, state.layers().size())), new Layer(block, 1));
        }
    }

    /** Adds a copy of the selected layer below it, as deep as the world allows, keeping the layer selected. */
    public record Duplicate() implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            int room = world.height() - state.height();
            if (state.selected() < 0 || room < 1) {
                return state;
            }
            Layer layer = state.layers().get(state.selected());
            Layer copy = new Layer(layer.block(), Math.min(layer.height(), room));
            return inserted(state, state.selected(), copy).select(state.selected() + 1);
        }
    }

    /** Removes the selected layer, selecting the one that was below it. */
    public record Remove() implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.selected() < 0) {
                return state;
            }
            List<Layer> layers = new ArrayList<>(state.layers());
            layers.remove(state.selected());
            return state.withLayers(layers, state.selected() - 1);
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
            int last = state.layers().size() - 1;
            if (state.selected() < 0 || to < 0 || to > last || to == state.selected()) {
                return state;
            }
            List<Layer> layers = new ArrayList<>(state.layers());
            layers.add(to, layers.remove(state.selected()));
            return state.withLayers(layers, to);
        }
    }

    /**
     * Makes the selected layer this many blocks thick, at least one and at most what the world allows.
     *
     * @param height the thickness asked for, in blocks
     */
    public record SetHeight(int height) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.selected() < 0) {
                return state;
            }
            int clamped = Math.max(1, Math.min(room(state, world), height));
            List<Layer> layers = new ArrayList<>(state.layers());
            layers.set(state.selected(), new Layer(layers.get(state.selected()).block(), clamped));
            return state.withLayers(layers, state.selected());
        }
    }

    /**
     * Makes the selected layer thicker or thinner by some blocks, within what {@link SetHeight} allows.
     *
     * @param blocks how many blocks thicker, negative for thinner
     */
    public record ChangeHeight(int blocks) implements Edit {
        @Override
        public EditorState apply(EditorState state, World world) {
            if (state.selected() < 0) {
                return state;
            }
            return new SetHeight(state.layers().get(state.selected()).height() + blocks).apply(state, world);
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
            int others = state.height() - state.layers().get(state.selected()).height();
            return new SetHeight(y - world.floor() - others).apply(state, world);
        }
    }

    /**
     * Puts other layers and a biome in place of the edited stack's, as pasting a superflat code does, and selects the
     * top layer; layers taller than the world are not put in.
     *
     * @param layers the new layers, bottom first
     * @param biome  the new biome's id
     */
    public record Replace(List<Layer> layers, String biome) implements Edit {
        public Replace {
            layers = List.copyOf(layers);
        }

        @Override
        public EditorState apply(EditorState state, World world) {
            EditorState replaced = state.withLayers(layers, layers.size() - 1).withBiome(biome);
            return replaced.fits(world) ? replaced : state;
        }
    }

    /** How deep the selected layer may be: the world's height less the layers around it. */
    public static int room(EditorState state, World world) {
        return world.height() - state.height() + state.layers().get(state.selected()).height();
    }

    private static EditorState inserted(EditorState state, int at, Layer layer) {
        List<Layer> layers = new ArrayList<>(state.layers());
        layers.add(at, layer);
        return state.withLayers(layers, at);
    }
}
