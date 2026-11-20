package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.BlockpilotApi;
import dev.blockpilot.api.CommandSpec;
import dev.flatgen.Flatgen;
import java.util.List;

/**
 * Flatgen's commands for Blockpilot (github.com/sparten11740/mc-blockpilot), which play tests and agents read and drive
 * Flatgen's editor with, registered under Flatgen's namespace while mods load. Each command is a class of its own,
 * listed here. Only touched when a Blockpilot new enough for them is installed (BlockpilotSupport).
 */
public final class BlockpilotCommands {
    private BlockpilotCommands() {
    }

    /** Registers every command. */
    static void register() {
        List<CommandSpec> commands =
                List.of(EditorQuery.SPEC, ColumnQuery.SPEC, WidgetsQuery.SPEC, PickerQuery.SPEC, NarrationQuery.SPEC,
                        OpenEditor.SPEC);
        for (CommandSpec command : commands) {
            BlockpilotApi.register(Flatgen.MOD_ID, command);
        }
    }
}
