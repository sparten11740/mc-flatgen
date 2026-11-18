package dev.flatgen.blocks.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hints that a block makes a poor layer, for the picker and the editor: what it is about the block, and what lies below
 * the layer, that the player may not expect.
 */
public final class LayerHints {
    private LayerHints() {
    }

    /** What makes a layer poor. */
    public enum Hint {
        /** Sand, gravel and the like fall when there is air, a fluid or the void below. */
        FALLS,
        /** Torches, flowers and the like are not whole blocks: nothing stands on them, and they may break. */
        NOT_WHOLE,
        /** Chests, signs and the like keep data in every block, which makes a world slow and large. */
        KEEPS_DATA
    }

    /**
     * What a block may be like as a layer.
     *
     * @param falls     whether it falls when nothing holds it up
     * @param whole     whether it fills its whole block
     * @param keepsData whether it is a block entity
     * @param holds     whether a falling block above it stays: not air, fire, a fluid or something it replaces, as the
     *                  game's own falling blocks decide
     */
    public record Traits(boolean falls, boolean whole, boolean keepsData, boolean holds) {
    }

    /** The hints for a layer of a block over what is below it, nothing below for the bottom layer. */
    public static List<Hint> of(Traits layer, Optional<Traits> below) {
        List<Hint> hints = new ArrayList<>();
        if (layer.falls() && !below.map(Traits::holds).orElse(false)) {
            hints.add(Hint.FALLS);
        }
        if (!layer.whole()) {
            hints.add(Hint.NOT_WHOLE);
        }
        if (layer.keepsData()) {
            hints.add(Hint.KEEPS_DATA);
        }
        return hints;
    }
}
