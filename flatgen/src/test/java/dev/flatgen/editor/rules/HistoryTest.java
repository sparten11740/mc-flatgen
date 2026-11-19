package dev.flatgen.editor.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.TestPresets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The editor's undo and redo. */
class HistoryTest {
    private static final World OVERWORLD = new World(-64, 384);
    private static final EditorState START = new EditorState(TestPresets.TALL_FLAT, Dimension.OVERWORLD, 1);
    /** An edit that empties the stack. */
    private static final Edit EMPTY = (state, world) -> state.withLayers(List.of(), -1);
    /** An edit that keeps only the bottom layer. */
    private static final Edit BOTTOM_ONLY = (state, world) ->
            state.withLayers(state.layers().subList(0, 1), 0);

    @Test
    void undoesAndRedoesAnEdit() {
        History history = new History(START);
        assertTrue(history.apply(EMPTY, OVERWORLD));
        assertTrue(history.canUndo());
        history.undo();
        assertEquals(START, history.current());
        assertTrue(history.canRedo());
        history.redo();
        assertEquals(0, history.current().preset().layers().size());
    }

    @Test
    void makesNoStepOfAnEditThatChangesNothing() {
        History history = new History(START);
        assertFalse(history.apply((state, world) -> state, OVERWORLD));
        assertFalse(history.canUndo());
    }

    @Test
    void forgetsWhatCouldBeRedoneOnceANewEditIsMade() {
        History history = new History(START);
        history.apply(EMPTY, OVERWORLD);
        history.undo();
        history.apply(BOTTOM_ONLY, OVERWORLD);
        assertFalse(history.canRedo());
    }

    @Test
    void choosingADimensionIsNoStepAndUndoGoesBackToWhereTheStepWasMade() {
        History history = new History(START);
        history.apply(BOTTOM_ONLY, OVERWORLD);
        history.choose(Dimension.NETHER);
        assertEquals(Dimension.NETHER, history.current().edited());
        history.undo();
        assertEquals(START, history.current());
        assertFalse(history.canUndo());
    }

    @Test
    void redoGoesToTheDimensionWhereTheStepWasMade() {
        History history = new History(START);
        history.apply(BOTTOM_ONLY, OVERWORLD);
        history.choose(Dimension.NETHER);
        history.undo();
        history.redo();
        assertEquals(Dimension.OVERWORLD, history.current().edited());
        assertEquals(1, history.current().preset().layers().size());
    }

    @Test
    void choosingTheDimensionAlreadyEditedKeepsTheSelection() {
        History history = new History(START);
        history.choose(Dimension.OVERWORLD);
        assertEquals(START, history.current());
    }

    @Test
    void selectingALayerIsNoStep() {
        History history = new History(START);
        history.select(3);
        assertEquals(3, history.current().selected());
        assertFalse(history.canUndo());
    }
}
