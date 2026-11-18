package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.Args;
import dev.blockpilot.api.CommandContext;
import dev.blockpilot.api.CommandSpec;
import dev.blockpilot.api.Fields;
import dev.blockpilot.api.Result;
import dev.flatgen.blocks.client.BlockPickerScreen;
import dev.flatgen.compat.blockpilot.rules.BlockRows;
import java.util.List;
import net.minecraft.client.Minecraft;

/** {@code flatgen:blocks}: the slots of the block picker's grid in sight, with their blocks and places. */
final class BlocksQuery {
    static final CommandSpec SPEC = CommandSpec.query("blocks", BlocksQuery::run)
            .help("the block picker's slots in sight: each one's block and where it is drawn")
            .field("open", "true while the block picker is the screen")
            .example("flatgen:blocks");

    private BlocksQuery() {
    }

    private static Result run(CommandContext context, Args args) {
        if (!(Minecraft.getInstance().screen instanceof BlockPickerScreen picker)) {
            return Result.ok(new Fields().put("open", false));
        }
        List<BlockRows.Slot> slots = picker.grid().slots().stream()
                .map(slot -> new BlockRows.Slot(slot.block().id(), slot.place())).toList();
        return Result.ok(new Fields().put("open", true))
                .withRows(BlockRows.of(slots, picker.grid().bounds()).stream().map(WidgetsQuery::toRow).toList());
    }
}
