package dev.flatgen.client.gui.rules;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Where an arrow moves the keyboard in a grid of cards laid out in rows: across to the card before or after, or up or
 * down to the nearest row, to its card nearest across. None at the grid's edge.
 */
public final class GridSteps {
    private GridSteps() {
    }

    /**
     * The card an arrow moves to.
     *
     * @param places where each card is, in the grid's order
     * @param from   the card the keyboard is on
     * @param across 1 for right, -1 for left, 0 for neither
     * @param down   1 for down, -1 for up, 0 for neither
     */
    public static OptionalInt next(List<Rect> places, int from, int across, int down) {
        if (across != 0) {
            int card = from + across;
            return card >= 0 && card < places.size() ? OptionalInt.of(card) : OptionalInt.empty();
        }
        if (down == 0 || from >= places.size()) {
            return OptionalInt.empty();
        }
        Rect start = places.get(from);
        OptionalInt rowY = IntStream.range(0, places.size()).map(card -> places.get(card).y())
                .filter(y -> down > 0 ? y > start.y() : y < start.y())
                .reduce((first, second) -> down > 0 ? Math.min(first, second) : Math.max(first, second));
        if (rowY.isEmpty()) {
            return OptionalInt.empty();
        }
        return IntStream.range(0, places.size()).filter(card -> places.get(card).y() == rowY.getAsInt()).boxed()
                .min(Comparator.comparingInt(card -> Math.abs(places.get(card).x() - start.x())))
                .map(OptionalInt::of).orElse(OptionalInt.empty());
    }
}
