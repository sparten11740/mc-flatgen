package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.Args;
import dev.blockpilot.api.CommandContext;
import dev.blockpilot.api.CommandSpec;
import dev.blockpilot.api.Fields;
import dev.blockpilot.api.Result;
import dev.blockpilot.api.Row;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.compat.blockpilot.rules.ReportRow;
import dev.flatgen.compat.blockpilot.rules.WidgetRows;
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * {@code flatgen:widgets}: the buttons, fields and other widgets of the Flatgen screen open now, which Blockpilot's own
 * {@code screen} cannot see, with where they are, so {@code click} can aim at them.
 */
final class WidgetsQuery {
    static final CommandSpec SPEC = CommandSpec.query("widgets", WidgetsQuery::run)
            .help("the widgets of the Flatgen screen open now: kind, label, bounds, whether usable, what they hold")
            .field("open", "true while a Flatgen screen is the screen")
            .example("flatgen:widgets");

    private WidgetsQuery() {
    }

    private static Result run(CommandContext context, Args args) {
        if (!(Minecraft.getInstance().screen instanceof FlatgenScreen screen)) {
            return Result.ok(new Fields().put("open", false));
        }
        List<Row> rows = WidgetRows.of(screen.describeWidgets()).stream().map(WidgetsQuery::toRow).toList();
        return Result.ok(new Fields().put("open", true)).withRows(rows);
    }

    /** A report row as Blockpilot's own. */
    static Row toRow(ReportRow row) {
        Fields fields = new Fields();
        row.fields().forEach(fields::put);
        return new Row(row.key(), fields);
    }
}
