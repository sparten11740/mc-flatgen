package dev.flatgen.blocks.client;

import dev.flatgen.blocks.rules.LayerHints;
import dev.flatgen.blocks.rules.SetBlock;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.preset.rules.Layer;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Choosing a layer's block, for the editor's layer cards: the picker that sets it, and its hints (falls, not a whole
 * block, keeps data), which say what the block will do in the stack.
 */
public final class BlockChoice {
    private BlockChoice() {
    }

    /** Opens the block picker for the selected layer, whose pick sets its block as one step to undo. */
    public static void pick(EditorContext editor) {
        choose(editor, block -> editor.apply(new SetBlock(block)));
    }

    /** Opens the block picker over the editor, handing the block picked to {@code onPick}; nothing when cancelled. */
    public static void choose(EditorContext editor, Consumer<String> onPick) {
        editor.open(BlockPicker.screen(editor.screen(), editor.enabledFeatures(), editor.registries(), onPick));
    }

    /** A layer's hints (falls, not a whole block, keeps data), each a few words, on one line; empty for none. */
    public static Component hints(EditorContext editor, int index) {
        List<Layer> layers = editor.shown().layers();
        Optional<LayerHints.Traits> below = index > 0
                ? Optional.of(BlockCatalog.traits(layers.get(index - 1).block())) : Optional.empty();
        MutableComponent line = Component.empty();
        for (LayerHints.Hint hint : LayerHints.of(BlockCatalog.traits(layers.get(index).block()), below)) {
            line.append(line.getSiblings().isEmpty() ? Component.empty() : Component.literal(" · "))
                    .append(Component.translatable(words(hint)));
        }
        return line;
    }

    /** A hint's words, by their translation key; the compiler holds every hint to having some. */
    private static String words(LayerHints.Hint hint) {
        return switch (hint) {
            case FALLS -> "flatgen.hint.falls";
            case NOT_WHOLE -> "flatgen.hint.not_whole";
            case KEEPS_DATA -> "flatgen.hint.keeps_data";
        };
    }
}
