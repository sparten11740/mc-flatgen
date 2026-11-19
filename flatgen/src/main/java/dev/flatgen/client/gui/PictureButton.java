package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import dev.flatgen.mc.UiSounds;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/** A button showing a picture in place of words, as the game's buttons look and sound; its tooltip says what it is. */
public final class PictureButton implements Widget {
    /** The border around the picture, in GUI pixels. */
    private static final int BORDER = 2;
    /** A picture button's size, in GUI pixels: a picture and its border. */
    public static final int SIZE = Icon.SIZE + 2 * BORDER;

    private final Icon icon;
    private final Rect bounds;
    private final Runnable action;
    private final Supplier<List<Component>> tooltip;

    /** A button showing this picture in this rectangle, doing {@code action} when clicked. */
    public PictureButton(Icon icon, Rect bounds, Runnable action, Supplier<List<Component>> tooltip) {
        this.icon = icon;
        this.bounds = bounds;
        this.action = action;
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        canvas.button(bounds, hovered, true);
        icon.draw(canvas, bounds.x() + (bounds.width() - Icon.SIZE) / 2,
                bounds.y() + (bounds.height() - Icon.SIZE) / 2);
        if (hovered) {
            canvas.tooltip(tooltip.get(), mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        if (button != MouseButtons.LEFT || !bounds.contains(x, y)) {
            return false;
        }
        UiSounds.click();
        action.run();
        return true;
    }

    @Override
    public List<WidgetDescription> describe() {
        List<Component> lines = tooltip.get();
        return List.of(new WidgetDescription("button", lines.isEmpty() ? "" : lines.get(0).getString(), bounds, true,
                ""));
    }
}
