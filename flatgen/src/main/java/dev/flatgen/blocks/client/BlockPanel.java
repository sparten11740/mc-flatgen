package dev.flatgen.blocks.client;

import dev.flatgen.blocks.rules.LayerHints;
import dev.flatgen.blocks.rules.SetBlock;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextLines;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.mc.Names;
import dev.flatgen.preset.rules.Layer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** The editor's block controls: a button showing the selected layer's block, which opens the picker, and its hints. */
public final class BlockPanel implements EditorPanel {
    /** The width of the block button, in GUI pixels: two of the panel's wide buttons and the gap between them. */
    private static final int BLOCK_BUTTON = 2 * PanelSizes.WIDE_BUTTON + PanelSizes.GAP;

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        flow.newRow();
        TextButton block = new TextButton(() -> label(editor), flow.next(BLOCK_BUTTON), () -> pick(editor),
                () -> editor.shown().selected() >= 0,
                () -> List.of(Component.translatable("flatgen.editor.pick_block")));
        return List.of(block, new TextLines(flow.line(editor.lineHeight()), () -> List.of(hints(editor))));
    }

    private static Component label(EditorContext editor) {
        int selected = editor.shown().selected();
        return selected >= 0 ? Names.block(editor.shown().preset().layers().get(selected).block())
                : Component.literal("-");
    }

    private static void pick(EditorContext editor) {
        editor.open(BlockPicker.screen(editor.screen(), editor.enabledFeatures(),
                block -> editor.apply(new SetBlock(block))));
    }

    /** The selected layer's hints (falls, not a whole block, keeps data), each a few words, on one line. */
    private static Component hints(EditorContext editor) {
        int selected = editor.shown().selected();
        if (selected < 0) {
            return Component.empty();
        }
        List<Layer> layers = editor.shown().preset().layers();
        Optional<LayerHints.Traits> below = selected > 0
                ? Optional.of(BlockCatalog.traits(layers.get(selected - 1).block())) : Optional.empty();
        MutableComponent line = Component.empty();
        for (LayerHints.Hint hint : LayerHints.of(BlockCatalog.traits(layers.get(selected).block()), below)) {
            line.append(line.getSiblings().isEmpty() ? Component.empty() : Component.literal(" · "))
                    .append(Component.translatable("flatgen.hint." + hint.name().toLowerCase(Locale.ROOT)));
        }
        return line;
    }
}
