package dev.flatgen.facts.rules;

import dev.flatgen.pictures.rules.Picture;
import java.util.List;
import java.util.Optional;

/**
 * One world fact (plan 3.4): what it shows of a world before it is made, as a picture and words, and the line it draws
 * across the editor's column, if any. Each fact is a class listed in {@link Facts}, so a new fact changes no other.
 */
public interface Fact {
    /** What the fact shows of this world. */
    Shown show(Situation situation);

    /** The line it draws across the column, if any. */
    default Optional<Marker> marker(Situation situation) {
        return Optional.empty();
    }

    /**
     * What a fact shows.
     *
     * @param icon  its picture: an item's id, or a 16 by 16 texture's location ending in {@code .png}
     * @param no    whether the picture is struck through, saying "no"
     * @param words what it says, a line each
     */
    record Shown(Picture icon, boolean no, List<Words> words) {
        public Shown {
            words = List.copyOf(words);
        }
    }

    /**
     * A line of a fact's words.
     *
     * @param key  its translation key
     * @param args the values it names, such as a Y
     */
    record Words(String key, List<Integer> args) {
        public Words {
            args = List.copyOf(args);
        }

        /** Words naming no values. */
        static Words of(String key) {
            return new Words(key, List.of());
        }
    }

    /**
     * A line across the column at a height that matters.
     *
     * @param y     the Y it is drawn at
     * @param color its colour, ARGB
     * @param icon  the picture of what it marks, drawn at its end: an item's id or a texture's location
     */
    record Marker(int y, int color, Picture icon) {
    }
}
