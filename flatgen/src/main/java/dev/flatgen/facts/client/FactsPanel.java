package dev.flatgen.facts.client;

import dev.flatgen.client.gui.Badge;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.facts.rules.NoSlimes;
import dev.flatgen.facts.rules.WorldFacts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;

/**
 * The editor's world facts (F08): pictures of the weather at the surface, slimes and monsters, each struck through
 * when it does not happen, with a tooltip in words, and a button that raises the surface out of slime chunks' reach.
 */
public final class FactsPanel implements EditorPanel {
    /** The picture of each weather: the sun for none, a bucket of water for rain, a snowball for snow. */
    private static final Map<WorldFacts.Weather, Icon> WEATHER = Map.of(
            WorldFacts.Weather.NONE, Icon.item("minecraft:sunflower"),
            WorldFacts.Weather.RAIN, Icon.item("minecraft:water_bucket"),
            WorldFacts.Weather.SNOW, Icon.item("minecraft:snowball"));
    private static final Icon SLIME = Icon.item("minecraft:slime_ball");
    private static final Icon MONSTER = Icon.item("minecraft:zombie_head");

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        FactsNow facts = new FactsNow(editor);
        flow.newRow();
        return List.of(
                new Badge(flow.next(Badge.SIZE), () -> facts.get().map(FactsPanel::weather)),
                new Badge(flow.next(Badge.SIZE), () -> facts.get().map(FactsPanel::slimes)),
                new Badge(flow.next(Badge.SIZE), () -> facts.get().map(FactsPanel::monsters)),
                new TextButton(Component.translatable("flatgen.facts.no_slimes"), flow.next(PanelSizes.WIDE_BUTTON),
                        () -> editor.apply(new NoSlimes()),
                        () -> facts.get().map(WorldFacts::slimeChunks).orElse(false),
                        () -> List.of(Component.translatable("flatgen.facts.no_slimes_tip",
                                WorldFacts.SLIME_CHUNK_MAX_Y))));
    }

    private static Badge.Look weather(WorldFacts facts) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("flatgen.facts.weather." + facts.weather().name().toLowerCase(Locale.ROOT)));
        facts.snowLine().ifPresent(y -> lines.add(Component.translatable("flatgen.facts.snow_line", y)));
        return new Badge.Look(WEATHER.get(facts.weather()), false, lines);
    }

    private static Badge.Look slimes(WorldFacts facts) {
        List<Component> lines = new ArrayList<>();
        if (facts.slimeChunks()) {
            lines.add(Component.translatable("flatgen.facts.slime_chunks", WorldFacts.SLIME_CHUNK_MAX_Y));
        }
        if (facts.surfaceSlimes()) {
            lines.add(Component.translatable("flatgen.facts.surface_slimes"));
        }
        boolean none = lines.isEmpty();
        if (none) {
            lines.add(Component.translatable("flatgen.facts.no_slimes_here"));
        }
        return new Badge.Look(SLIME, none, lines);
    }

    private static Badge.Look monsters(WorldFacts facts) {
        return new Badge.Look(MONSTER, !facts.hostileMobs(), List.of(Component.translatable(
                facts.hostileMobs() ? "flatgen.facts.monsters" : "flatgen.facts.no_monsters")));
    }
}
