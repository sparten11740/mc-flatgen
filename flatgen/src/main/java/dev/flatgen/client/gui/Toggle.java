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
 * A switch with a picture, a name, and a knob that sits at the right while it is on and at the left while it is off,
 * so a player who cannot read, or tells no green from grey, sees both what it is and whether it is on. A click turns
 * it over; its tooltip says what it does. It can be unusable, drawn greyed, with the tooltip saying why.
 */
public final class Toggle implements Control {
    /** Space around the icon and the switch, and between the icon and the name, in GUI pixels. */
    private static final int PADDING = 2;
    /** A switch's track, and the knob that sits at its left while off and its right while on, in GUI pixels. */
    private static final int SWITCH_WIDTH = 17;
    private static final int SWITCH_HEIGHT = 10;
    private static final int KNOB_SIZE = 8;

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
        Rect picture = new Rect(iconX, TextStyle.BODY.pictureTop(bounds.y(), bounds.height()), Icon.SIZE, Icon.SIZE);
        icon.draw(canvas, picture.x(), picture.y());
        if (!canUse) {
            canvas.dim(picture);
        }
        int stateX = bounds.right() - 2 * PADDING - SWITCH_WIDTH;
        int labelX = iconX + Icon.SIZE + PADDING;
        canvas.text(canvas.fitted(label, stateX - PADDING - labelX, TextStyle.BODY), labelX,
                TextStyle.BODY.top(bounds.y(), bounds.height()), canUse ? Look.TEXT : Look.DISABLED);
        drawSwitch(canvas, stateX, bounds.y() + (bounds.height() - SWITCH_HEIGHT) / 2, on.getAsBoolean());
        List<Component> lines = tooltip.get();
        if (hovered && !lines.isEmpty()) {
            canvas.tooltipBelow(lines, bounds);
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

    /** How wide a toggle is whose label is this wide, in GUI pixels: its picture, the label and the switch. */
    public static int width(int labelWidth) {
        return 2 * PADDING + Icon.SIZE + PADDING + labelWidth + 3 * PADDING + SWITCH_WIDTH;
    }

    /** Draws a switch, green with its knob at the right while on, grey with it at the left while off. */
    private static void drawSwitch(Canvas canvas, int x, int y, boolean on) {
        canvas.sprite(on ? Look.SWITCH_ON : Look.SWITCH_OFF, new Rect(x, y, SWITCH_WIDTH, SWITCH_HEIGHT));
        int inset = (SWITCH_HEIGHT - KNOB_SIZE) / 2;
        int knobX = on ? x + SWITCH_WIDTH - inset - KNOB_SIZE : x + inset;
        canvas.sprite(Look.KNOB, new Rect(knobX, y + inset, KNOB_SIZE, KNOB_SIZE));
    }
}
