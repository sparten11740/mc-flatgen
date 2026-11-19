package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.Args;
import dev.blockpilot.api.CommandContext;
import dev.blockpilot.api.CommandSpec;
import dev.blockpilot.api.Fields;
import dev.blockpilot.api.Result;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.compat.blockpilot.rules.PickerRows;
import java.util.List;
import net.minecraft.client.Minecraft;

/** {@code flatgen:picker}: the cards of the open picker's grid in sight, the block or biome picker's. */
final class PickerQuery {
    static final CommandSpec SPEC = CommandSpec.query("picker", PickerQuery::run)
            .help("the open picker's cards in sight (the block or biome picker's): each one's id and where it is drawn")
            .field("open", "true while a picker is the screen")
            .field("title", "the picker's title")
            .example("flatgen:picker");

    private PickerQuery() {
    }

    private static Result run(CommandContext context, Args args) {
        if (!(Minecraft.getInstance().screen instanceof PickerScreen picker)) {
            return Result.ok(new Fields().put("open", false));
        }
        List<PickerRows.Card> cards = picker.grid().cards().stream()
                .map(card -> new PickerRows.Card(card.entry().id(), picker.inGameGui(card.place()))).toList();
        return Result.ok(new Fields().put("open", true).put("title", picker.getTitle().getString()))
                .withRows(PickerRows.of(cards, picker.inGameGui(picker.grid().bounds())).stream()
                        .map(WidgetsQuery::toRow).toList());
    }
}
