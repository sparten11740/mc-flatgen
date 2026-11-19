package dev.flatgen.library;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * How often the player's presets or choices have been written since the game started, so what shows them (the
 * editor's favourites) reads them again only after a change.
 */
public final class LibraryChanges {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private LibraryChanges() {
    }

    /** Notes that a preset file or the choices were written or deleted. */
    static void changed() {
        COUNT.incrementAndGet();
    }

    /** How many changes there have been; another number than last time means something changed. */
    public static int count() {
        return COUNT.get();
    }
}
