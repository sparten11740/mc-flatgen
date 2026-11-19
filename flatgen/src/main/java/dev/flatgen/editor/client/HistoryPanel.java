package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Undo and redo, as buttons and as Control+Z, Control+Y and Control+Shift+Z. */
final class HistoryPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        flow.newRow();
        return List.of(
                new PictureButton(Icon.own("undo"), flow.next(PictureButton.SIZE), editor::undo, editor::canUndo,
                        () -> List.of(Component.translatable("flatgen.editor.undo"))),
                new PictureButton(Icon.own("redo"), flow.next(PictureButton.SIZE), editor::redo, editor::canRedo,
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
