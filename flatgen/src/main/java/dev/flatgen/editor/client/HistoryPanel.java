package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.editor.rules.EditorState;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Undo and redo, as buttons and as Control+Z, Control+Y and Control+Shift+Z. */
final class HistoryPanel implements EditorPanel {
    /** Shown for every dimension, with a stack or without (F22). */
    @Override
    public boolean shows(EditorState state) {
        return true;
    }

    /** Undo, with its name, and Redo, at the left of the footer. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        layout.button(EditorRegion.FOOTER, Icon.line("undo"), Component.translatable("flatgen.editor.undo"),
                editor::undo, editor::canUndo, List::of);
        layout.add(EditorRegion.FOOTER, new PictureButton(Icon.line("redo"), layout.flow(EditorRegion.FOOTER)
                .next(EditorRegion.FOOTER.rowHeight()), editor::redo, editor::canRedo,
                () -> List.of(Component.translatable("flatgen.editor.redo"))));
    }

    @Override
    public boolean shortcut(EditorContext editor, int keyCode, Modifiers modifiers) {
        boolean redo = keyCode == GLFW.GLFW_KEY_Y || (keyCode == GLFW.GLFW_KEY_Z && modifiers.shift());
        if (!modifiers.control() || (keyCode != GLFW.GLFW_KEY_Z && keyCode != GLFW.GLFW_KEY_Y)) {
            return false;
        }
        if (redo) {
            editor.redo();
        } else {
            editor.undo();
        }
        return true;
    }
}
