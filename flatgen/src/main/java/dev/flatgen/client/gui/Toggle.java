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
 * A switch with a picture, a name, and a box that is ticked while it is on and empty while it is off, so a player who
 * cannot read, or tells no green from grey, sees both what it is and whether it is on. A click turns it over; its
 * tooltip says what it does. It can be unusable, drawn greyed, with the tooltip saying why.
 */
public final class Toggle implements Control {
    /** Space around the icon and the state's picture, and between the icon and the name, in GUI pixels. */
    private static final int PADDING = 2;
    /** Whether it is on, as a shape and not a colour alone: a ticked box, or an empty one. */
    private static final Icon ON = Icon.own("switch_on");
    private static final Icon OFF = Icon.own("switch_off");
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int DISABLED_LABEL_COLOR = 0xFFA0A0A0;

    private final Icon icon;
    private final Component label;
    private final Rect bounds;
    private final BooleanSupplier on;
    private final Runnable turnOver;
    private final BooleanSupplier usable;
    private final Supplier<List<Component>> tooltip;

    /**
     * A switch showing a picture and a name, on while {@code on} says so, turned over by {@code turnOver}.
     *
     * @param usable  whether it can be turned over now
     * @param tooltip what it does, or why it cannot be turned over; empty for none
     */
    public Toggle(Icon icon, Component label, Rect bounds, BooleanSupplier on, Runnable turnOver,
            BooleanSupplier usable, Supplier<List<Component>> tooltip) {
        this.icon = icon;
        this.label = label;
        this.bounds = bounds;
        this.on = on;
        this.turnOver = turnOver;
        this.usable = usable;
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean canUse = usable.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        canvas.button(bounds, hovered, canUse);
        int iconX = bounds.x() + PADDING;
        Rect picture = new Rect(iconX, bounds.y() + (bounds.height() - Icon.SIZE) / 2, Icon.SIZE, Icon.SIZE);
        icon.draw(canvas, picture.x(), picture.y());
        if (!canUse) {
            canvas.dim(picture);
        }
        int stateX = bounds.right() - PADDING - Icon.SIZE;
        int labelX = iconX + Icon.SIZE + PADDING;
        canvas.clip(new Rect(labelX, bounds.y(), stateX - labelX, bounds.height()));
        canvas.text(label, labelX, bounds.y() + (bounds.height() - canvas.lineHeight()) / 2 + 1,
                canUse ? LABEL_COLOR : DISABLED_LABEL_COLOR);
        canvas.unclip();
        (on.getAsBoolean() ? ON : OFF).draw(canvas, stateX, bounds.y() + (bounds.height() - Icon.SIZE) / 2);
        List<Component> lines = tooltip.get();
        if (hovered && !lines.isEmpty()) {
            canvas.tooltip(lines, mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return button == MouseButtons.LEFT && bounds.contains(x, y) && Control.use(usable, turnOver);
    }

    /** Enter or Space uses it while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Keys.uses(keyCode) && Control.use(usable, turnOver);
    }

    @Override
    public List<Component> tooltip() {
        return tooltip.get();
    }

    @Override
    public Optional<Narration> narration() {
        return Optional.of(Narration.toggle(label, on.getAsBoolean(), usable.getAsBoolean(), tooltip.get()));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("toggle", label.getString(), bounds, usable.getAsBoolean(),
                Boolean.toString(on.getAsBoolean())));
    }
}
