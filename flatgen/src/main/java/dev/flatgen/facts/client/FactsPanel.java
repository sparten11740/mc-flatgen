package dev.flatgen.facts.client;

import dev.flatgen.client.gui.Badge;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.facts.rules.Facts;
import java.util.List;

/**
 * The editor's world facts (F08): a picture for each of {@link Facts#ALL}, struck through when it says no, with its
 * words as a tooltip.
 */
public final class FactsPanel implements EditorPanel {
    /** Shown for every dimension the preset gives a stack of its own (F22). */
    @Override
    public boolean shows(EditorContext editor) {
        return editor.shown().stack().isPresent();
    }

    /** What the world will be like, as pictures, on a box of the aside. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.box(inside -> content(editor, inside));
    }

    private List<Widget> content(EditorContext editor, FlowLayout flow) {
        FactsNow now = new FactsNow(editor);
        return Facts.ALL.stream().<Widget>map(fact -> new Badge(flow.next(Badge.SIZE),
                () -> now.get().map(situation -> FactPictures.look(fact.show(situation))))).toList();
    }
}
