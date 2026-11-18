package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.SetBiome;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.facts.client.FactsNow;
import dev.flatgen.mc.Names;
import java.util.List;
import net.minecraft.network.chat.Component;

/** The editor's biome control: a button showing the world's biome, which opens the biome picker. */
public final class BiomePanel implements EditorPanel {
    /** The width of the biome button, in GUI pixels: two of the panel's wide buttons and the gap between them. */
    private static final int BIOME_BUTTON = 2 * PanelSizes.WIDE_BUTTON + PanelSizes.GAP;

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        flow.newRow();
        return List.of(new TextButton(() -> Names.biome(editor.shown().preset().biome()), flow.next(BIOME_BUTTON),
                () -> pick(editor), () -> true, () -> List.of(Component.translatable("flatgen.editor.pick_biome"))));
    }

    private static void pick(EditorContext editor) {
        editor.open(BiomePicker.screen(editor.screen(), editor.registries(), new FactsNow(editor)::withBiome,
                biome -> editor.apply(new SetBiome(biome))));
    }
}
