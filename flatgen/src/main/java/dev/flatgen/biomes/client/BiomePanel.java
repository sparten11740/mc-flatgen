package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.SetBiome;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.dimensions.client.DimensionPictures;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.facts.client.FactsNow;
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

    /** The world's biome, a button with its picture and name on a box of the aside. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.box(inside -> content(editor, inside));
    }

    private List<Widget> content(EditorContext editor, FlowLayout flow) {
        Map<String, Icon> pictures = new HashMap<>();
        Icon picture = (canvas, x, y) -> pictures.computeIfAbsent(editor.shown().biome(),
                biome -> BiomePicker.picture(biome, editor.registries())).draw(canvas, x, y);
        return List.of(new Caption(() -> Component.translatable("flatgen.editor.biome"),
                        flow.line(TextStyle.BODY.lineHeight()), TextStyle.BODY, Look.MUTED),
                new TextButton(picture, () -> Names.biome(editor.shown().biome()),
                        flow.line(EditorRegion.ASIDE.rowHeight()), () -> pick(editor), () -> true,
                        () -> List.of(Component.translatable("flatgen.editor.pick_biome"))));
    }

    private static void pick(EditorContext editor) {
        Component now = Component.translatable("flatgen.biomes.now", DimensionPictures.name(editor.shown().edited()));
        editor.open(BiomePicker.screen(editor.screen(), editor.registries(), new FactsNow(editor)::withBiome, now,
                editor.shown().biome(), biome -> editor.apply(new SetBiome(biome))));
    }
}
