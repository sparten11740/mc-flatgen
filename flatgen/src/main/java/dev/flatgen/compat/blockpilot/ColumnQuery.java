package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.Args;
import dev.blockpilot.api.CommandContext;
import dev.blockpilot.api.CommandSpec;
import dev.blockpilot.api.Fields;
import dev.blockpilot.api.Result;
import dev.blockpilot.api.Row;
import dev.flatgen.compat.blockpilot.rules.ColumnRows;
import dev.flatgen.editor.client.EditorScreen;
import dev.flatgen.layers.client.ColumnWidget;
import java.util.List;
import net.minecraft.client.Minecraft;

/** {@code flatgen:column}: each layer of the editor's column, with where it is drawn. */
final class ColumnQuery {
    static final CommandSpec SPEC = CommandSpec.query("column", ColumnQuery::run)
            .help("each layer in Flatgen's column, top first: its block, height, Ys and where it is drawn")
            .field("open", "true while Flatgen's editor is the screen")
            .example("flatgen:column");

    private ColumnQuery() {
    }

    private static Result run(CommandContext context, Args args) {
        if (!(Minecraft.getInstance().screen instanceof EditorScreen editor)) {
            return Result.ok(new Fields().put("open", false));
        }
        ColumnWidget column = editor.column();
        List<Row> rows = ColumnRows.of(editor.shown(), editor.worldBottom(), column.layerBounds()).stream()
                .map(WidgetsQuery::toRow).toList();
        return Result.ok(new Fields().put("open", true)).withRows(rows);
    }
}
