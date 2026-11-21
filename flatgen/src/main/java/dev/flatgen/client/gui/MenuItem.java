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
 * A row of a menu ({@link MenuScreen}): a picture and its name from the left, lit under the mouse; a click does what it
 * says. It can be unusable for a while, drawn greyed, with its tooltip saying why.
 */
public final class MenuItem implements Control {
    /** Space between the row's left edge and the picture, and between the picture and the name, in GUI pixels. */
    private static final int INSET = 5;
    private static final int GAP = 5;

    private final Icon icon;
    private final Supplier<Component> label;
    private final Rect bounds;
    private final Runnable action;
    private final BooleanSupplier usable;
    private final Supplier<List<Component>> tooltip;

    /**
     * A row showing this picture and name, doing {@code action} when clicked.
     *
     * @param usable  whether it can be used now
     * @param tooltip what it does, or why it cannot be used; empty for none
     */
    public MenuItem(Icon icon, Supplier<Component> label, Rect bounds, Runnable action, BooleanSupplier usable,
            Supplier<List<Component>> tooltip) {
        this.icon = icon;
        this.label = label;
        this.bounds = bounds;
        this.action = action;
        this.usable = usable;
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean canUse = usable.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        if (hovered && canUse) {
            canvas.sprite(Look.BUTTON_LIT, bounds);
        }
        int iconX = bounds.x() + INSET;
        Rect picture = new Rect(iconX, TextStyle.BODY.pictureTop(bounds.y(), bounds.height()), icon.width(),
                Icon.SIZE);
        icon.draw(canvas, picture.x(), picture.y());
        if (!canUse) {
            canvas.dim(picture);
        }
        int labelX = picture.right() + GAP;
        canvas.text(canvas.fitted(label.get(), bounds.right() - INSET - labelX, TextStyle.BODY), labelX,
                TextStyle.BODY.top(bounds.y(), bounds.height()), canUse ? Look.TEXT : Look.DISABLED);
        List<Component> lines = tooltip.get();
        if (hovered && !lines.isEmpty()) {
            canvas.tooltipBelow(lines, bounds);
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
        return Optional.of(Narration.button(label.get(), usable.getAsBoolean(), tooltip.get()));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("button", label.get().getString(), bounds, usable.getAsBoolean(), ""));
    }
}
