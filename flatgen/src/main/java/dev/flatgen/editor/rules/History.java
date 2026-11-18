package dev.flatgen.editor.rules;

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

    /** Goes one step back, if there is one. */
    public void undo() {
        if (!undoable.isEmpty()) {
            redoable.push(current);
            current = undoable.pop();
        }
    }

    /** Makes the step last undone again, if there is one. */
    public void redo() {
        if (!redoable.isEmpty()) {
            undoable.push(current);
            current = redoable.pop();
        }
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
