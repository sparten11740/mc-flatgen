package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.Args;
import dev.blockpilot.api.CommandContext;
import dev.blockpilot.api.CommandSpec;
import dev.blockpilot.api.Fields;
import dev.blockpilot.api.Result;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.mc.Screens;

/**
 * {@code flatgen:narration}: what the Flatgen screen open now hands the game's narrator, for the control with the
 * focus or else the widget under the mouse, so a script can check what a player who listens hears.
 */
final class NarrationQuery {
    static final CommandSpec SPEC = CommandSpec.query("narration", NarrationQuery::run)
            .help("what the Flatgen screen open now tells the narrator: the focused control's, else the hovered one's")
            .field("open", "true while a Flatgen screen is the screen")
            .field("said", "the words, joined by \" | \"")
            .example("flatgen:narration");

    private NarrationQuery() {
    }

    private static Result run(CommandContext context, Args args) {
        if (!(Screens.current() instanceof FlatgenScreen screen)) {
            return Result.ok(new Fields().put("open", false));
        }
        return Result.ok(new Fields().put("open", true).put("said", String.join(" | ", screen.narratedNow())));
    }
}
