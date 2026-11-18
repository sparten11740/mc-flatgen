package dev.flatgen.layers.client;

import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;

/** What the column asks of the editor it sits in: what to draw, and the selections and edits a player makes on it. */
public interface ColumnActions {
    /** What the column draws: the editor's state, with an edit being previewed made on it. */
    EditorState shown();

    /** Selects a layer, bottom first. */
    void select(int layer);

    /** Makes an edit, a step to undo. */
    void apply(Edit edit);

    /** Shows an edit made on the editor's state without making it, in place of any shown before, while a drag lasts. */
    void preview(Edit edit);

    /** Makes the edit being previewed, as one step to undo. */
    void commitPreview();
}
