package dev.flatgen.facts.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.facts.rules.NoSlimes;
import dev.flatgen.facts.rules.WorldFacts;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * The No slimes button, which raises the surface out of slime chunks' reach: on a box of its own under the facts, as it
 * does something where the facts only tell, and shown only while slimes spawn in slime chunks at the surface.
 */
public final class NoSlimesPanel implements EditorPanel {
    /** The button's picture: the slimes fact's slime with an up arrow, for raising the ground above them. */
    private static final Icon NO_SLIMES = Icon.marked(Icon.own("slime"), "arrow_up");

    /** Shown while the shown stack's surface is low enough for slimes to spawn in slime chunks. */
    @Override
    public boolean shows(EditorContext editor) {
        return editor.shown().stack().isPresent()
                && new FactsNow(editor).get().map(situation -> situation.facts().slimeChunks()).orElse(false);
    }

    /** The button, its tooltip saying where it raises the surface to. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.buttonBox(NO_SLIMES, Component.translatable("flatgen.facts.no_slimes"),
                () -> editor.apply(new NoSlimes()),
                () -> List.of(Component.translatable("flatgen.facts.no_slimes_tip", WorldFacts.SLIME_CHUNK_MAX_Y)));
    }
}
