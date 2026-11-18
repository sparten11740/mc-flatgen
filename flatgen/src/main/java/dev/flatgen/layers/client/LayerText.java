package dev.flatgen.layers.client;

import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.mc.Names;
import dev.flatgen.preset.rules.Layer;
import java.util.List;
import net.minecraft.network.chat.Component;

/** How a layer is told to the player, alike in the column's tooltip and in the editor's panel. */
public final class LayerText {
    private LayerText() {
    }

    /** A layer's block, its depth and the Ys it covers, a line each. */
    public static List<Component> describe(EditorState state, World world, int index) {
        Layer layer = state.preset().layers().get(index);
        return List.of(Names.block(layer.block()), Component.translatable("flatgen.layer.height", layer.height()),
                yRange(state, world, index));
    }

    /** The Ys a layer covers, bottom to top. */
    public static Component yRange(EditorState state, World world, int index) {
        List<Layer> layers = state.preset().layers();
        int bottomY = world.floor() + layers.subList(0, index).stream().mapToInt(Layer::height).sum();
        return Component.translatable("flatgen.layer.y_range", bottomY, bottomY + layers.get(index).height() - 1);
    }
}
