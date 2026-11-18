package dev.flatgen.options;

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

    /** Whether options were applied to this screen before. */
    static boolean has(CreateWorldScreen screen) {
        return APPLIED.containsKey(screen);
    }

    /** Remembers the options applied to this screen. */
    static void remember(CreateWorldScreen screen, Map<String, Boolean> options) {
        APPLIED.put(screen, Map.copyOf(options));
    }
}
