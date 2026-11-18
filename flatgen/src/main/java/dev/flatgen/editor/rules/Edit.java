package dev.flatgen.editor.rules;

/**
 * A change to what the editor holds, which the {@link History} can undo. Each kind of change is a class of its own, so
 * features add theirs without changing the editor; an edit that cannot be made returns the state it was given.
 */
public interface Edit {
    /** The editor's state after this change, in a world it keeps the stack within. */
    EditorState apply(EditorState state, World world);
}
