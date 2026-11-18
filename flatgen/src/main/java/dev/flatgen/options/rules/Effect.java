package dev.flatgen.options.rules;

import java.util.Map;

/**
 * One thing a world option does to the world being created, a kind of its own read by {@link EffectKinds}. A kind acts
 * when the editor's Done is pressed, on the world's first start, or both, through what Flatgen hands it there.
 */
public interface Effect {
    /** What it does to the world on the Create World screen when Done is pressed. */
    default void atDone(WorldSetup setup) {
    }

    /** What it does once, when the world first starts. */
    default void atFirstStart(FirstStart start) {
    }

    /** Whether it acts on the world's first start, which Flatgen must then remember for the world. */
    default boolean actsAtFirstStart() {
        return false;
    }

    /**
     * The single settings it fixes, each by a name of its own with the value it gives it; an option fixing one of them
     * to another value clashes with this one.
     */
    default Map<String, String> settings() {
        return Map.of();
    }
}
