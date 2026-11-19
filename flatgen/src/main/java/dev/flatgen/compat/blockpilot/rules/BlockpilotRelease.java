package dev.flatgen.compat.blockpilot.rules;

import java.util.Arrays;

/**
 * Which Blockpilot releases Flatgen's commands work with: the release whose API they are built against and later ones.
 * An older Blockpilot is left alone, so it never stops the game.
 */
public final class BlockpilotRelease {
    /** The first release the commands work with; flatgen/gradle.properties names it too, as blockpilot_release. */
    public static final String FIRST = "1.4.0";

    private BlockpilotRelease() {
    }

    /**
     * Whether a Blockpilot of this version, as its loader reports it ("1.4.0+1.21.1-neoforge"), is the first release
     * or a later one; a version that does not start with numbers is not.
     */
    public static boolean supported(String installed) {
        int[] have = numbers(installed);
        int[] need = numbers(FIRST);
        for (int part = 0; part < need.length; part++) {
            int own = part < have.length ? have[part] : 0;
            if (own != need[part]) {
                return own > need[part];
            }
        }
        return have.length > 0;
    }

    /** The numbers a version starts with, "1.4.0+1.21.1" giving 1, 4 and 0; none when it starts with none. */
    private static int[] numbers(String version) {
        String release = version.split("[^0-9.]", 2)[0];
        return release.isEmpty() ? new int[0]
                : Arrays.stream(release.split("\\.")).filter(part -> !part.isEmpty())
                        .mapToInt(Integer::parseInt).toArray();
    }
}
