package dev.flatgen.options.rules;

/** A block's position, in a world or within a template, in blocks. */
public record Position(int x, int y, int z) {
    /** This position moved by another. */
    public Position plus(Position other) {
        return new Position(x + other.x, y + other.y, z + other.z);
    }
}
