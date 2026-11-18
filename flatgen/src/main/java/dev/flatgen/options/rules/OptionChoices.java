package dev.flatgen.options.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Which world options a preset has on. A preset names only those it sets otherwise than their default (F02's
 * {@code options}), so a mod that adds an option changes no preset; an option that is no longer installed stays named.
 */
public final class OptionChoices {
    private OptionChoices() {
    }

    /** Whether an option is on: as the preset names it, or else as its default. */
    public static boolean isOn(WorldOption option, Map<String, Boolean> chosen) {
        return chosen.getOrDefault(option.id(), option.defaultOn());
    }

    /**
     * The choices once an option is turned on or off; turned on, every option that clashes with it goes off. Each is
     * named only where it differs from its default, and options no longer installed stay as they were.
     */
    public static Map<String, Boolean> switched(Map<String, Boolean> chosen, WorldOption option, boolean on,
            List<WorldOption> installed) {
        Map<String, Boolean> switched = new TreeMap<>(chosen);
        set(switched, option, on);
        for (WorldOption other : installed) {
            if (on && !other.id().equals(option.id()) && option.clashesWith(other)) {
                set(switched, other, false);
            }
        }
        return switched;
    }

    /** The installed options that are on, in the order given. */
    public static List<WorldOption> on(Map<String, Boolean> chosen, List<WorldOption> installed) {
        return installed.stream().filter(option -> isOn(option, chosen)).toList();
    }

    /**
     * What options do at Done, gathered.
     *
     * @param gameRules     the game rules they set, by name; a later option's value wins
     * @param structureSets the structure sets they add, by id
     */
    public record Applied(Map<String, String> gameRules, Set<String> structureSets) {
    }

    /** What these options do at Done, in their order. */
    public static Applied applied(List<WorldOption> options) {
        Applied applied = new Applied(new LinkedHashMap<>(), new LinkedHashSet<>());
        WorldSetup setup = new WorldSetup() {
            @Override
            public void setGameRule(String rule, String value) {
                applied.gameRules().put(rule, value);
            }

            @Override
            public void addStructureSets(List<String> sets) {
                applied.structureSets().addAll(sets);
            }
        };
        options.forEach(option -> option.effects().forEach(effect -> effect.atDone(setup)));
        return new Applied(Collections.unmodifiableMap(applied.gameRules()),
                Collections.unmodifiableSet(applied.structureSets()));
    }

    /**
     * The choices as the world's game rules show them: an option that only sets game rules is on exactly when they are
     * as it sets them, and one that does more (sets the clock, places a template) goes off when they are not, never on,
     * as the rules alone do not give what it does. Options that set no game rules stay as they were chosen.
     *
     * @param gameRules the world's game rules now, by name, their values as the game writes them
     */
    public static Map<String, Boolean> followingGameRules(Map<String, Boolean> chosen, List<WorldOption> installed,
            Map<String, String> gameRules) {
        Map<String, Boolean> following = new TreeMap<>(chosen);
        for (WorldOption option : installed) {
            Applied applied = applied(List.of(option));
            boolean onlyRules = option.effects().stream().allMatch(Effect::onlySetsGameRules);
            boolean rulesAsSet = gameRules.entrySet().containsAll(applied.gameRules().entrySet());
            if (!applied.gameRules().isEmpty() && (onlyRules || !rulesAsSet)) {
                set(following, option, rulesAsSet && (onlyRules || isOn(option, chosen)));
            }
        }
        return following;
    }

    /** The ids a preset names that no installed option has, in id order. */
    public static List<String> missing(Map<String, Boolean> chosen, List<WorldOption> installed) {
        List<String> missing = new ArrayList<>(new TreeMap<>(chosen).keySet());
        installed.forEach(option -> missing.remove(option.id()));
        return missing;
    }

    private static void set(Map<String, Boolean> chosen, WorldOption option, boolean on) {
        if (on == option.defaultOn()) {
            chosen.remove(option.id());
        } else {
            chosen.put(option.id(), on);
        }
    }
}
