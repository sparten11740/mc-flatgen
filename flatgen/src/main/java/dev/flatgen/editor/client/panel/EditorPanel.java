package dev.flatgen.editor.client.panel;

import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Modifiers;
import java.nio.file.Path;
import java.util.List;

/**
 * A part of the panel beside the editor's column: its widgets, placed in rows that wrap within the panel, and keys of
 * its own. Each feature that adds controls to the editor brings a panel, listed in the editor's table of panels, so
 * adding one never changes the editor or another panel.
 */
public interface EditorPanel {
    /** The panel's widgets, placed by the flow, which carries on below the panel before it. */
    List<Widget> layOut(EditorContext editor, FlowLayout flow);

    /** Files the player dropped on the editor; whether this panel took them. */
    default boolean filesDropped(EditorContext editor, List<Path> files) {
        return false;
    }

    /** A key no widget took; whether this panel used it. */
    default boolean shortcut(EditorContext editor, int keyCode, Modifiers modifiers) {
        return false;
    }
}
