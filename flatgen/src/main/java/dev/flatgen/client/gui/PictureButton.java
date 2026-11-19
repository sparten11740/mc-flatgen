package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A button showing a picture in place of words, as the game's buttons look and sound; its tooltip says what it is.
 * It can be unusable for a while, its picture then dimmed, its tooltip saying why.
 */
public final class PictureButton implements Control {
    /** The border around the picture, in GUI pixels. */
    private static final int BORDER = 2;
    /** A picture button's size, in GUI pixels: a picture and its border. */
    public static final int SIZE = Icon.SIZE + 2 * BORDER;

    private final Icon icon;
    private final Rect bounds;
    private final Runnable action;
    private final BooleanSupplier usable;
    private final Supplier<List<Component>> tooltip;

    /** A button showing this picture in this rectangle, always usable, doing {@code action} when clicked. */
    public PictureButton(Icon icon, Rect bounds, Runnable action, Supplier<List<Component>> tooltip) {
        this(icon, bounds, action, () -> true, tooltip);
    }

    /** A button showing this picture in this rectangle, usable while {@code usable} says so. */
    public PictureButton(Icon icon, Rect bounds, Runnable action, BooleanSupplier usable,
            Supplier<List<Component>> tooltip) {
        this.icon = icon;
        this.bounds = bounds;
        this.action = action;
        this.usable = usable;
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        boolean canUse = usable.getAsBoolean();
        canvas.button(bounds, hovered, canUse);
        Rect picture = new Rect(bounds.x() + (bounds.width() - Icon.SIZE) / 2,
                bounds.y() + (bounds.height() - Icon.SIZE) / 2, Icon.SIZE, Icon.SIZE);
        icon.draw(canvas, picture.x(), picture.y());
        if (!canUse) {
            canvas.dim(picture);
        }
        if (hovered) {
            canvas.tooltip(tooltip.get(), mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return button == MouseButtons.LEFT && bounds.contains(x, y) && Control.use(usable, action);
    }

    /** Enter or Space uses it while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Keys.uses(keyCode) && Control.use(usable, action);
    }

    @Override
    public List<Component> tooltip() {
        return tooltip.get();
    }

    @Override
    public Optional<Narration> narration() {
        return Optional.of(Narration.button(tooltip.get(), usable.getAsBoolean()));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        List<Component> lines = tooltip.get();
        return List.of(new WidgetDescription("button", lines.isEmpty() ? "" : lines.get(0).getString(), bounds,
                usable.getAsBoolean(), ""));
    }
}
