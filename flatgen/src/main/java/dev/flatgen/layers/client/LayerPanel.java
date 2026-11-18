package dev.flatgen.layers.client;

import dev.flatgen.client.gui.NumberField;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextLines;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.editor.rules.Edit;
import dev.flatgen.layers.rules.LayerEdits;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The editor's layer controls: the selected layer's Ys; its depth, one less, one more, typed or one of the quick
 * depths; the surface's Y to type; adding, copying and removing layers; and their keys (arrows, Alt with arrows,
 * Delete, Insert, Control+D).
 */
public final class LayerPanel implements EditorPanel {
    /** The depths a single click sets the selected layer to. */
    private static final List<Integer> QUICK_HEIGHTS = List.of(1, 2, 3, 5, 10, 50, 100);
    /** Widths of the controls, in GUI pixels: a sign, a quick depth and a number to type. */
    private static final int SMALL = 20;
    private static final int QUICK = 22;
    private static final int FIELD = 40;

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        Panel panel = new Panel(editor);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new TextLines(flow.line(editor.lineHeight()), panel::layerLine));
        flow.newRow();
        widgets.add(panel.button("−", flow.next(SMALL), new LayerEdits.ChangeHeight(-1),
                () -> panel.hasLayer() && panel.height() > 1, () -> panel.tip("flatgen.layer.shallower")));
        widgets.add(new NumberField("height", flow.next(FIELD), panel::height,
                height -> editor.apply(new LayerEdits.SetHeight(height))));
        widgets.add(panel.button("+", flow.next(SMALL), new LayerEdits.ChangeHeight(1),
                () -> panel.hasLayer() && panel.height() < panel.room(),
                () -> panel.hasLayer() && panel.height() >= panel.room() ? panel.full()
                        : panel.tip("flatgen.layer.deeper")));
        for (int quick : QUICK_HEIGHTS) {
            widgets.add(panel.button(Integer.toString(quick), flow.next(QUICK), new LayerEdits.SetHeight(quick),
                    panel::hasLayer, List::of));
        }
        flow.newRow();
        widgets.addAll(surfaceRow(editor, flow));
        flow.newRow();
        Supplier<List<Component>> fullTip = () -> panel.roomLeft() > 0 ? List.of() : panel.full();
        widgets.add(panel.button(Component.translatable("flatgen.editor.add_above"), flow.next(PanelSizes.WIDE_BUTTON),
                new LayerEdits.Add(true), () -> panel.roomLeft() > 0, fullTip));
        widgets.add(panel.button(Component.translatable("flatgen.editor.add_below"), flow.next(PanelSizes.WIDE_BUTTON),
                new LayerEdits.Add(false), () -> panel.roomLeft() > 0, fullTip));
        widgets.add(panel.button(Component.translatable("flatgen.editor.duplicate"), flow.next(PanelSizes.WIDE_BUTTON),
                new LayerEdits.Duplicate(), () -> panel.hasLayer() && panel.roomLeft() > 0, fullTip));
        widgets.add(panel.button(Component.translatable("flatgen.editor.remove"), flow.next(PanelSizes.WIDE_BUTTON),
                new LayerEdits.Remove(), panel::hasLayer, List::of));
        return widgets;
    }

    @Override
    public boolean shortcut(EditorContext editor, int keyCode, Modifiers modifiers) {
        int selected = editor.shown().selected();
        Runnable action = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> modifiers.alt() ? () -> editor.apply(new LayerEdits.Move(selected + 1))
                    : () -> editor.select(selected + 1);
            case GLFW.GLFW_KEY_DOWN -> modifiers.alt() ? () -> editor.apply(new LayerEdits.Move(selected - 1))
                    : () -> editor.select(Math.max(0, selected - 1));
            case GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE -> () -> editor.apply(new LayerEdits.Remove());
            case GLFW.GLFW_KEY_INSERT -> () -> editor.apply(new LayerEdits.Add(true));
            case GLFW.GLFW_KEY_D -> modifiers.control() ? () -> editor.apply(new LayerEdits.Duplicate()) : null;
            default -> null;
        };
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }

    /** The surface's Y to type, with its label: the selected layer's depth changes so the surface is there. */
    private static List<Widget> surfaceRow(EditorContext editor, FlowLayout flow) {
        Component label = Component.translatable("flatgen.editor.surface_field");
        Rect place = flow.next(editor.textWidth(label));
        Rect text = new Rect(place.x(), place.y() + (place.height() - editor.lineHeight()) / 2, place.width(),
                editor.lineHeight());
        return List.of(new TextLines(text, () -> List.of(label)),
                new NumberField("surface", flow.next(FIELD),
                        () -> editor.world().floor() + editor.shown().preset().height(),
                        y -> editor.apply(new LayerEdits.SurfaceAt(y))));
    }

    /** What the controls read of the editor, worked out when asked, as it changes. */
    private record Panel(EditorContext editor) {
        boolean hasLayer() {
            return editor.shown().selected() >= 0;
        }

        int height() {
            return hasLayer() ? editor.shown().preset().layers().get(editor.shown().selected()).height() : 0;
        }

        int room() {
            return LayerEdits.room(editor.shown(), editor.world());
        }

        int roomLeft() {
            return editor.world().height() - editor.shown().preset().height();
        }

        List<Component> layerLine() {
            return List.of(hasLayer() ? LayerText.yRange(editor.shown(), editor.world(), editor.shown().selected())
                    : Component.translatable("flatgen.editor.empty"));
        }

        List<Component> tip(String key) {
            return List.of(Component.translatable(key));
        }

        /** Why a control that adds blocks cannot: the stack fills the world. */
        List<Component> full() {
            return List.of(Component.translatable("flatgen.editor.full", editor.world().height()));
        }

        TextButton button(String label, Rect place, Edit edit, BooleanSupplier active,
                Supplier<List<Component>> tooltip) {
            return button(Component.literal(label), place, edit, active, tooltip);
        }

        TextButton button(Component label, Rect place, Edit edit, BooleanSupplier active,
                Supplier<List<Component>> tooltip) {
            return new TextButton(label, place, () -> editor.apply(edit), active, tooltip);
        }
    }
}
