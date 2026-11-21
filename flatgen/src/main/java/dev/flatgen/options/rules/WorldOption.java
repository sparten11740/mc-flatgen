package dev.flatgen.options.rules;

import dev.flatgen.pictures.rules.Picture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A switch in Flatgen's editor that does something to the world being created, added by a mod or pack as a file
 * {@code assets/<namespace>/flatgen/options/<name>.json} (docs/world-options.md).
 *
 * @param id          {@code <namespace>:<name>}
 * @param name        its name, a translation key or the text itself
 * @param description what it does, a translation key or the text itself
 * @param icon        its picture
 * @param group       the group it is shown in: a mod's id, or {@code <namespace>:<path>} for a group of its own
 * @param groupIcon   the picture of its group's heading, if it gives one; else the group shows its first option's
 * @param defaultOn   whether it is on unless a preset says otherwise
 * @param inverted    whether its switch shows it the other way round, on while it is off, so a switch that stops
 *                    something shows that thing, on as the world has it
 * @param requires    the mods it needs, by id; it is hidden while one is missing
 * @param effects     what it does, in order
 */
public record WorldOption(String id, String name, String description, Picture icon, String group,
        Optional<Picture> groupIcon, boolean defaultOn, boolean inverted, List<String> requires, List<Effect> effects) {
    public WorldOption {
        requires = List.copyOf(requires);
        effects = List.copyOf(effects);
    }

    /** Whether it fixes a setting that another option fixes otherwise, so the two cannot be on together. */
    public boolean clashesWith(WorldOption other) {
        Map<String, String> others = settings(other);
        return settings(this).entrySet().stream()
                .anyMatch(setting -> others.containsKey(setting.getKey())
                        && !others.get(setting.getKey()).equals(setting.getValue()));
    }

    /** Whether one of its effects acts on the world's first start. */
    public boolean actsAtFirstStart() {
        return effects.stream().anyMatch(Effect::actsAtFirstStart);
    }

    private static Map<String, String> settings(WorldOption option) {
        Map<String, String> settings = new HashMap<>();
        option.effects().forEach(effect -> settings.putAll(effect.settings()));
        return settings;
    }
}
