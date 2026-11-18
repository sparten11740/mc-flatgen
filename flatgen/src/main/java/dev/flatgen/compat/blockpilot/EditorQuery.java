package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.Args;
import dev.blockpilot.api.CommandContext;
import dev.blockpilot.api.CommandSpec;
import dev.blockpilot.api.Fields;
import dev.blockpilot.api.Result;
import dev.flatgen.compat.blockpilot.rules.EditorFields;
import dev.flatgen.editor.client.EditorScreen;
import net.minecraft.client.Minecraft;

/** {@code flatgen:editor}: whether Flatgen's editor is open, and the preset it holds. */
final class EditorQuery {
    static final CommandSpec SPEC = CommandSpec.query("editor", EditorQuery::run)
            .help("whether Flatgen's editor is open, and the preset it holds")
            .field("open", "true while Flatgen's editor is the screen")
            .field("code", "the layers bottom up and the biome, as the game's superflat code")
            .field("layers", "how many layers")
            .field("height", "the stack's height in blocks")
            .field("surface_y", "the Y mobs stand at, on the top layer")
            .field("biome", "the biome's id")
            .example("flatgen:editor");

    private EditorQuery() {
    }

    private static Result run(CommandContext context, Args args) {
        Fields fields = new Fields();
        if (Minecraft.getInstance().screen instanceof EditorScreen editor) {
            EditorFields.of(editor.preset(), editor.worldBottom()).forEach(fields::put);
        } else {
            fields.put("open", false);
        }
        return Result.ok(fields);
    }
}
