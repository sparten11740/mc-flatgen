package dev.flatgen.structures;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

/**
 * The Create World screens still open on which Flatgen's last Done turned the game's Generate Structures off, so that a
 * later Done with structures, or the world leaving Superflat, turns it on again; a screen closed is forgotten.
 */
public final class GenerateStructuresSessions {
    private static final Set<CreateWorldScreen> TURNED_OFF = Collections.newSetFromMap(new WeakHashMap<>());

    private GenerateStructuresSessions() {
    }

    /** Whether Flatgen's last Done turned Generate Structures off on this screen. */
    public static boolean turnedOff(CreateWorldScreen screen) {
        return TURNED_OFF.contains(screen);
    }

    /** Remembers whether the last Done on this screen turned Generate Structures off. */
    public static void remember(CreateWorldScreen screen, boolean turnedOff) {
        if (turnedOff) {
            TURNED_OFF.add(screen);
        } else {
            TURNED_OFF.remove(screen);
        }
    }
}
