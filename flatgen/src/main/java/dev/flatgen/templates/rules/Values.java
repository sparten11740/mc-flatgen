package dev.flatgen.templates.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The values of a template's settings, by id, as the player has set them.
 *
 * @param numbers  the numbers
 * @param blocks   the blocks, by id
 * @param toggles  the switches
 */
public record Values(Map<String, Integer> numbers, Map<String, String> blocks, Map<String, Boolean> toggles) {
    public Values {
        numbers = Map.copyOf(numbers);
        blocks = Map.copyOf(blocks);
        toggles = Map.copyOf(toggles);
    }

    /** Every setting at its default. */
    public static Values defaults(List<Parameter> parameters) {
        Defaults defaults = new Defaults();
        parameters.forEach(parameter -> parameter.accept(defaults));
        return new Values(defaults.numbers, defaults.blocks, defaults.toggles);
    }

    /** Gathers each setting's default by its kind. */
    private static final class Defaults implements Parameter.Visitor<Void> {
        private final Map<String, Integer> numbers = new HashMap<>();
        private final Map<String, String> blocks = new HashMap<>();
        private final Map<String, Boolean> toggles = new HashMap<>();

        @Override
        public Void number(Parameter.Number number) {
            numbers.put(number.id(), number.defaultValue());
            return null;
        }

        @Override
        public Void block(Parameter.Block block) {
            blocks.put(block.id(), block.defaultBlock());
            return null;
        }

        @Override
        public Void toggle(Parameter.Toggle toggle) {
            toggles.put(toggle.id(), toggle.defaultValue());
            return null;
        }
    }

    /** A number's value. */
    public int number(Parameter.Number number) {
        return numbers.getOrDefault(number.id(), number.defaultValue());
    }

    /** A block's value. */
    public String block(Parameter.Block block) {
        return blocks.getOrDefault(block.id(), block.defaultBlock());
    }

    /** A switch's value. */
    public boolean toggle(Parameter.Toggle toggle) {
        return toggles.getOrDefault(toggle.id(), toggle.defaultValue());
    }

    /** These values with a number set, kept within its range. */
    public Values with(Parameter.Number number, int value) {
        Map<String, Integer> changed = new HashMap<>(numbers);
        changed.put(number.id(), number.within(value));
        return new Values(changed, blocks, toggles);
    }

    /** These values with a block set. */
    public Values with(Parameter.Block block, String value) {
        Map<String, String> changed = new HashMap<>(blocks);
        changed.put(block.id(), value);
        return new Values(numbers, changed, toggles);
    }

    /** These values with a switch set. */
    public Values with(Parameter.Toggle toggle, boolean value) {
        Map<String, Boolean> changed = new HashMap<>(toggles);
        changed.put(toggle.id(), value);
        return new Values(numbers, blocks, changed);
    }
}
