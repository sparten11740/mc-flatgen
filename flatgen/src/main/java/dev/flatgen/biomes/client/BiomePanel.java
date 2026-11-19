package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.SetBiome;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.facts.client.FactsNow;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Names;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

/** The editor's biome control: a button showing the world's biome, which opens the biome picker. */
public final class BiomePanel implements EditorPanel {
    /** Shown for every dimension the preset gives a stack of its own (F22). */
    @Override
    public boolean shows(EditorState state) {
        return state.stack().isPresent();
    }

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        flow.newRow();
        Map<String, Icon> pictures = new HashMap<>();
        Icon picture = (canvas, x, y) -> pictures.computeIfAbsent(editor.shown().biome(),
                biome -> picture(biome, editor)).draw(canvas, x, y);
        return List.of(new TextButton(picture, () -> Names.biome(editor.shown().biome()),
                flow.next(PanelSizes.DOUBLE_WIDTH), () -> pick(editor), () -> true,
                () -> List.of(Component.translatable("flatgen.editor.pick_biome"))));
    }

    /** A biome's landscape in its colours, worked out once per biome while the editor is open. */
    private static Icon picture(String biome, EditorContext editor) {
        return Biomes.info(biome, editor.registries()).<Icon>map(info -> new BiomeIcon(info.colours()))
                .orElse(Icon.MISSING);
    }

    private static void pick(EditorContext editor) {
        editor.open(BiomePicker.screen(editor.screen(), editor.registries(), new FactsNow(editor)::withBiome,
                biome -> editor.apply(new SetBiome(biome))));
    }
}
