package dev.flatgen.options;

import dev.flatgen.mc.GameRuleAccess;
import dev.flatgen.options.rules.OptionChoices;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

/**
 * The world options last applied to each Create World screen still open, so Flatgen's editor reopens with the
 * switches as the player left them; a screen closed is forgotten.
 */
public final class OptionSessions {
    private static final Map<CreateWorldScreen, Map<String, Boolean>> APPLIED = new WeakHashMap<>();

    private OptionSessions() {
    }

    /** The options last applied to this screen, set otherwise than their default; none before the first Done. */
    public static Map<String, Boolean> applied(CreateWorldScreen screen) {
        return APPLIED.getOrDefault(screen, Map.of());
    }

    /**
     * The options Flatgen's editor shows for this screen: their defaults before the first Done, and after it those
     * last applied, with each option that sets game rules on exactly when the screen's game rules are as it sets
     * them, as its own game rules screen has the last word (F10).
     */
    public static Map<String, Boolean> shown(CreateWorldScreen screen) {
        if (!has(screen)) {
            return Map.of();
        }
        return OptionChoices.followingGameRules(applied(screen), InstalledOptions.load(),
                GameRuleAccess.values(screen));
    }

    /** Whether options were applied to this screen before. */
    static boolean has(CreateWorldScreen screen) {
        return APPLIED.containsKey(screen);
    }

    /** Remembers the options applied to this screen. */
    static void remember(CreateWorldScreen screen, Map<String, Boolean> options) {
        APPLIED.put(screen, Map.copyOf(options));
    }
}
