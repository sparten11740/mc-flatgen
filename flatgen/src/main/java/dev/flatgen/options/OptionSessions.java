package dev.flatgen.options;

import dev.flatgen.mc.GameRuleAccess;
import dev.flatgen.options.rules.AddStructures;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

/**
 * What Flatgen last applied to each Create World screen still open, so its editor reopens as the player left it; a
 * screen closed is forgotten.
 */
public final class OptionSessions {
    private static final Map<CreateWorldScreen, Applied> APPLIED = new WeakHashMap<>();

    private OptionSessions() {
    }

    /**
     * What the last Done applied to a screen.
     *
     * @param options   the options, set otherwise than their default
     * @param addedSets the structure sets options added beyond those the player chose
     */
    private record Applied(Map<String, Boolean> options, Set<String> addedSets) {
    }

    /** The options last applied to this screen, set otherwise than their default; none before the first Done. */
    public static Map<String, Boolean> applied(CreateWorldScreen screen) {
        Applied applied = APPLIED.get(screen);
        return applied == null ? Map.of() : applied.options();
    }

    /**
     * The preset Flatgen's editor shows for the world on this screen, read from its settings: before the first Done
     * as it is; after it with the options last applied, each that sets game rules shown as the screen's game rules
     * are, as its own game rules screen has the last word (F10), and without the structure sets options added, which
     * show as theirs.
     */
    public static Preset shown(CreateWorldScreen screen, Preset fromWorld, List<WorldOption> installed) {
        Applied applied = APPLIED.get(screen);
        if (applied == null) {
            return fromWorld;
        }
        return AddStructures.removedFrom(fromWorld, applied.addedSets()).withOptions(OptionChoices.followingGameRules(
                applied.options(), installed, GameRuleAccess.values(screen)));
    }

    /** Whether options were applied to this screen before. */
    static boolean has(CreateWorldScreen screen) {
        return APPLIED.containsKey(screen);
    }

    /** Forgets what Done applied to this screen, as when it leaves Superflat. */
    static void forget(CreateWorldScreen screen) {
        APPLIED.remove(screen);
    }

    /** Remembers what Done applied to this screen. */
    static void remember(CreateWorldScreen screen, Map<String, Boolean> options, Set<String> addedSets) {
        APPLIED.put(screen, new Applied(Map.copyOf(options), Set.copyOf(addedSets)));
    }
}
