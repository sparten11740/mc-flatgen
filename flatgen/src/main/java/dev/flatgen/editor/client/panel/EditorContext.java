package dev.flatgen.editor.client.panel;

import dev.flatgen.editor.rules.ApplyPreset;
import dev.flatgen.editor.rules.Edit;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.editor.rules.World;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.Stack;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;

/**
 * What the editor's column and panels see of the editor: what it holds, the world it is for, and the changes they make
 * to it. Every change goes through the editor's history.
 */
public interface EditorContext {
    /** What the editor shows: its state, with an edit being previewed made on it. */
    EditorState shown();

    /** The world the edited stack stands in: its dimension's floor and height. */
    World world();

    /** The world a dimension's stack stands in. */
    World world(Dimension dimension);

    /** Makes an edit, a step to undo. Every change, undo and redo too, clears the message. */
    void apply(Edit edit);

    /** Selects a layer, bottom first, which is no step. */
    void select(int layer);

    /** Shows and edits this dimension's stack (F22), its top layer selected, which is no step. */
    void choose(Dimension dimension);

    /** Shows an edit made on the editor's state without making it, in place of any shown before, while a drag lasts. */
    void preview(Edit edit);

    /** Makes the edit being previewed, as one step to undo. */
    void commitPreview();

    /** Goes one step back. */
    void undo();

    /** Makes the step last undone again. */
    void redo();

    /** Whether there is a step to undo. */
    boolean canUndo();

    /** Whether there is a step to redo. */
    boolean canRedo();

    /** Shows a message in the panel until the next change. */
    void say(Component message);

    /**
     * Puts a whole preset in the editor as one step to undo, a saved one or one a template made; one with a stack
     * taller than its dimension is left out, and the editor's line says so.
     */
    default void use(Preset preset) {
        Optional<World> tooSmall = Arrays.stream(Dimension.values())
                .filter(dimension -> preset.stack(dimension).map(Stack::height).orElse(0) > world(dimension).height())
                .findFirst().map(this::world);
        if (tooSmall.isPresent()) {
            say(Component.translatable("flatgen.editor.too_tall", tooSmall.get().height()));
            return;
        }
        apply(new ApplyPreset(preset));
    }

    /** The message shown now, if any; null for none. */
    Component message();

    /** The world options installed, read once when the editor opened. */
    List<WorldOption> worldOptions();

    /** The registries of the world being created, which ids resolve against. */
    HolderLookup.Provider registries();

    /** The features the world being created allows. */
    FeatureFlagSet enabledFeatures();

    /** The editor's own screen, to return to from a screen it opens. */
    Screen screen();

    /** Opens another screen in place of the editor. */
    void open(Screen screen);

    /** How wide text is drawn, for laying out a label. */
    int textWidth(Component text);

    /** How tall a line of text is. */
    int lineHeight();
}
