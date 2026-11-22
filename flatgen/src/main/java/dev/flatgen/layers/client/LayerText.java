package dev.flatgen.layers.client;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.mc.Names;
import dev.flatgen.preset.rules.Layer;
import java.util.List;
import net.minecraft.network.chat.Component;

/** How a layer is told to the player, alike in the column's tooltip and on the layer cards. */
public final class LayerText {
    private LayerText() {
    }

    /** A layer's block, its depth and the Ys it covers, a line each. */
    public static List<Component> describe(EditorState state, World world, int index) {
        Layer layer = state.layers().get(index);
        return List.of(Names.block(layer.block()), Component.translatable("flatgen.layer.thickness", layer.height()),
                yRange(state, world, index));
    }

    /** How many more blocks fit in the world above the stack. */
    public static int roomLeft(EditorState state, World world) {
        return world.height() - state.height();
    }

    /** Why a control that adds blocks cannot: none while there is room, else that the stack fills the world. */
    public static List<Component> whyFull(EditorState state, World world) {
        return roomLeft(state, world) > 0 ? List.of()
                : List.of(Component.translatable("flatgen.editor.full", world.height()));
    }

    /** The blocks of air above ground this many blocks tall. */
    public static Component air(World world, int groundHeight) {
        return Component.translatable("flatgen.editor.air", world.height() - groundHeight);
    }

    /** The Ys a layer covers, bottom to top. */
    public static Component yRange(EditorState state, World world, int index) {
        List<Layer> layers = state.layers();
        int bottomY = world.floor() + layers.subList(0, index).stream().mapToInt(Layer::height).sum();
        return Component.translatable("flatgen.layer.y_range", bottomY, bottomY + layers.get(index).height() - 1);
    }
}
