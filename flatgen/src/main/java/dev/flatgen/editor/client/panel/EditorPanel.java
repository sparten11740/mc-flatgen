package dev.flatgen.editor.client.panel;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.preset.rules.Dimension;
import java.nio.file.Path;
import java.util.List;

/**
 * A part of the editor: its widgets, placed in the editor's regions, and keys of its own. Each feature that adds
 * controls to the editor brings a panel, listed in the editor's table of panels, so adding one never changes the
 * editor or another panel.
 */
public interface EditorPanel {
    /**
     * Whether the panel shows for what the editor holds now; by default only while the overworld is edited, as a panel
     * that changes the whole preset needs (F22).
     */
    default boolean shows(EditorContext editor) {
        return editor.shown().edited() == Dimension.OVERWORLD;
    }

    /**
     * Places the panel's widgets in the editor's regions, each after those of the panels before it; what shows only
     * in Advanced is placed while {@link EditorContext#advanced()} says so.
     */
    void layOut(EditorContext editor, EditorLayout layout);

    /** Files the player dropped on the editor; whether this panel took them. */
    default boolean filesDropped(EditorContext editor, List<Path> files) {
        return false;
    }

    /** A key no widget took; whether this panel used it. */
    default boolean shortcut(EditorContext editor, int keyCode, Modifiers modifiers) {
        return false;
    }
}
