package dev.flatgen.editor.rules;

import dev.flatgen.preset.rules.Dimension;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The editor's undo and redo: every edit that changes something is a step back, and undoing a step lets it be redone
 * until a new edit is made. Selecting another layer is not a step.
 */
public final class History {
    /** How many steps back are kept; older ones are forgotten. */
    private static final int MOST_STEPS = 200;

    private final Deque<EditorState> undoable = new ArrayDeque<>();
    private final Deque<EditorState> redoable = new ArrayDeque<>();
    private EditorState current;

    public History(EditorState start) {
        this.current = start;
    }

    /** What the editor holds now. */
    public EditorState current() {
        return current;
    }

    /** Makes an edit; whether it changed anything, and so became a step to undo. */
    public boolean apply(Edit edit, World world) {
        EditorState next = edit.apply(current, world);
        if (next.equals(current)) {
            return false;
        }
        undoable.push(current);
        if (undoable.size() > MOST_STEPS) {
            undoable.removeLast();
        }
        redoable.clear();
        current = next;
        return true;
    }

    /** Selects a layer, which is not a step. */
    public void select(int layer) {
        current = current.select(layer);
    }

    /**
     * Edits another dimension's stack, its top layer selected, which is not a step: undo and redo go to the dimension
     * where each step was made. Choosing the dimension already edited changes nothing.
     */
    public void choose(Dimension dimension) {
        if (dimension != current.edited()) {
            current = current.choose(dimension);
        }
    }

    /** Goes one step back, if there is one, to the dimension where it was made. */
    public void undo() {
        if (!undoable.isEmpty()) {
            EditorState back = undoable.pop();
            redoable.push(inDimensionOf(current, back));
            current = back;
        }
    }

    /** Makes the step last undone again, if there is one, in the dimension where it was made. */
    public void redo() {
        if (!redoable.isEmpty()) {
            EditorState ahead = redoable.pop();
            undoable.push(inDimensionOf(current, ahead));
            current = ahead;
        }
    }

    /**
     * A state kept to go back or ahead to, in the dimension of the step beside it, so a dimension chosen after a step
     * is not where undo or redo lands.
     */
    private static EditorState inDimensionOf(EditorState state, EditorState step) {
        return state.edited() == step.edited() ? state : state.choose(step.edited());
    }

    /** Whether there is a step to undo. */
    public boolean canUndo() {
        return !undoable.isEmpty();
    }

    /** Whether there is a step to redo. */
    public boolean canRedo() {
        return !redoable.isEmpty();
    }
}
