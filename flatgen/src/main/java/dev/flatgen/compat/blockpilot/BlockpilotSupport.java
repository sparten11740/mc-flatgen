package dev.flatgen.compat.blockpilot;

import dev.flatgen.Flatgen;
import dev.flatgen.compat.blockpilot.rules.BlockpilotRelease;

/**
 * Whether Flatgen registers its Blockpilot commands, told by each loader's entry point which Blockpilot is installed:
 * with the release they are built against or a later one; an older one is left alone and logged, so it never stops
 * the game. It names nothing of Blockpilot's own, so with an older Blockpilot none of that is loaded.
 */
public final class BlockpilotSupport {
    /**
     * Blockpilot's mod id, which each loader's entry point looks for. A compile-time constant, so reading it loads
     * nothing.
     */
    public static final String MOD_ID = "blockpilot";

    private BlockpilotSupport() {
    }

    /** Blockpilot of this version, as its loader reports it, is installed: registers the commands if it can. */
    public static void installed(String version) {
        if (BlockpilotRelease.supported(version)) {
            BlockpilotCommands.register();
        } else {
            Flatgen.LOGGER.info("Blockpilot {} is older than {}: Flatgen's commands for it are left out", version,
                    BlockpilotRelease.FIRST);
        }
    }
}
