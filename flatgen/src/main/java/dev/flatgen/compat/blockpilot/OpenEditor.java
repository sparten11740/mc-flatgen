package dev.flatgen.compat.blockpilot;

import dev.blockpilot.api.Action;
import dev.blockpilot.api.Args;
import dev.blockpilot.api.CommandContext;
import dev.blockpilot.api.CommandException;
import dev.blockpilot.api.CommandSpec;
import dev.blockpilot.api.Result;
import dev.flatgen.editor.client.PresetEditors;
import dev.flatgen.mc.WorldCreation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * {@code flatgen:open}: from the title screen or the world list, Create World with Superflat chosen and Flatgen's editor
 * open on it, so a play test starts in the editor in one step.
 */
final class OpenEditor {
    static final CommandSpec SPEC = CommandSpec.action("open", OpenEditor::start)
            .help("opens Create World with Superflat chosen and Flatgen's editor on it, from outside a world")
            .example("flatgen:open");

    private OpenEditor() {
    }

    private static Action start(CommandContext context, Args args) throws CommandException {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            throw new CommandException("in_world", "leave the world first");
        }
        CreateWorldScreen screen = WorldCreation.openWithWorldType(minecraft, minecraft.screen, WorldPresets.FLAT);
        minecraft.setScreen(PresetEditors.EDITOR.createEditScreen(screen, screen.getUiState().getSettings()));
        return Result::ok;
    }
}
