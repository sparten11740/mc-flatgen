package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.BlockpilotApi;
import dev.blockpilot.api.CommandSpec;
import dev.flatgen.Flatgen;
import java.util.List;

/**
 * Flatgen's commands for Blockpilot (github.com/sparten11740/mc-blockpilot), which play tests and agents read and drive
 * Flatgen's editor with, registered under Flatgen's namespace while mods load. Each command is a class of its own,
 * listed here. Only touched when Blockpilot is installed.
 */
public final class BlockpilotCommands {
    /**
     * Blockpilot's mod id, which each loader's entry point checks before registering. A compile-time constant, so
     * checking it loads nothing of this class or of Blockpilot's.
     */
    public static final String MOD_ID = "blockpilot";

    private BlockpilotCommands() {
    }

    /** Registers every command. */
    public static void register() {
        for (CommandSpec command : List.of(EditorQuery.SPEC, OpenEditor.SPEC)) {
            BlockpilotApi.register(Flatgen.MOD_ID, command);
        }
    }
}
