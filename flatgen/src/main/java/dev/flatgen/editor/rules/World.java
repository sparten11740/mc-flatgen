package dev.flatgen.editor.rules;

/**
 * The world a stack stands in, which edits keep it within.
 *
 * @param floor  the Y of its lowest block, where the bottom layer starts
 * @param height how many blocks tall it is
 */
public record World(int floor, int height) {
    /** The Y of its highest block. */
    public int top() {
        return floor + height - 1;
    }
}
