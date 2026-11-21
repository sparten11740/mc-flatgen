package dev.flatgen.templates.rules;

import dev.flatgen.pictures.rules.Picture;
/**
 * One of a template's settings, which its screen shows as a slider, a block button or a switch. The kinds are few and
 * fixed; a screen tells them apart through {@link Visitor} rather than by testing classes.
 */
public interface Parameter {
    /** The setting's id, by which its value is kept. */
    String id();

    /** Its name, a translation key. */
    String name();

    /** Tells the visitor which kind this is. */
    <R> R accept(Visitor<R> visitor);

    /** What each kind of parameter becomes, as a screen's control. */
    interface Visitor<R> {
        R number(Number number);

        R block(Block block);

        R toggle(Toggle toggle);
    }

    /**
     * A whole number within a range, as a height or a count.
     *
     * @param id           the setting's id
     * @param name         its name, a translation key
     * @param icon         what its slider shows: an item's id, or a texture's location ending in {@code .png}
     * @param min          the least it may be
     * @param max          the most it may be
     * @param defaultValue what it starts at
     */
    record Number(String id, String name, Picture icon, int min, int max, int defaultValue) implements Parameter {
        /** A value kept within the range. */
        public int within(int value) {
            return Math.max(min, Math.min(max, value));
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.number(this);
        }
    }

    /**
     * A block, as a layer's.
     *
     * @param id           the setting's id
     * @param name         its name, a translation key
     * @param defaultBlock the block it starts as
     */
    record Block(String id, String name, String defaultBlock) implements Parameter {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.block(this);
        }
    }

    /**
     * On or off.
     *
     * @param id           the setting's id
     * @param name         its name, a translation key
     * @param icon         what its switch shows: an item's id, or a texture's location ending in {@code .png}
     * @param defaultValue whether it starts on
     */
    record Toggle(String id, String name, Picture icon, boolean defaultValue) implements Parameter {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.toggle(this);
        }
    }
}
