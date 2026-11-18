package dev.flatgen.facts.client;

import dev.flatgen.client.gui.Badge;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.facts.rules.Fact;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.facts.rules.NoSlimes;
import dev.flatgen.facts.rules.WorldFacts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * The editor's world facts (F08): a picture for each of {@link Facts#ALL}, struck through when it says no, with its
 * words as a tooltip, and a button that raises the surface out of slime chunks' reach.
 */
public final class FactsPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        FactsNow now = new FactsNow(editor);
        flow.newRow();
        List<Widget> widgets = new ArrayList<>();
        Facts.ALL.forEach(fact -> widgets.add(new Badge(flow.next(Badge.SIZE),
                () -> now.get().map(situation -> look(fact.show(situation))))));
        widgets.add(new TextButton(Component.translatable("flatgen.facts.no_slimes"),
                flow.next(PanelSizes.WIDE_BUTTON), () -> editor.apply(new NoSlimes()),
                () -> now.get().map(situation -> situation.facts().slimeChunks()).orElse(false),
                () -> List.of(Component.translatable("flatgen.facts.no_slimes_tip", WorldFacts.SLIME_CHUNK_MAX_Y))));
        return widgets;
    }

    /** What a fact shows, as a badge draws it. */
    private static Badge.Look look(Fact.Shown shown) {
        return new Badge.Look(FactPictures.icon(shown), FactPictures.words(shown));
    }
}
