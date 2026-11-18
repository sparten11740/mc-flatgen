package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import dev.flatgen.mc.UiSounds;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A switch with a picture, a name, and a light that is green while it is on and dark while it is off,
 * so a player who cannot read sees both what it is and whether it is on. A click turns it over; its tooltip says what
 * it does. It can be unusable, drawn greyed, with the tooltip saying why.
 */
public final class Toggle implements Widget {
    /** Space around the icon and the light, and between the icon and the name, in GUI pixels. */
    private static final int PADDING = 2;
    /** The size of the light, in GUI pixels. */
    private static final int LIGHT = 6;
    private static final int LIGHT_ON = 0xFF40D040;
    private static final int LIGHT_OFF = 0xFF303030;
    private static final int LIGHT_RIM = 0xFF000000;
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
        icon.draw(canvas, iconX, bounds.y() + (bounds.height() - Icon.SIZE) / 2);
        Rect light = new Rect(bounds.right() - PADDING - LIGHT - 1, bounds.y() + (bounds.height() - LIGHT) / 2, LIGHT,
                LIGHT);
        int labelX = iconX + Icon.SIZE + PADDING;
        canvas.clip(new Rect(labelX, bounds.y(), light.x() - PADDING - labelX, bounds.height()));
        canvas.text(label, labelX, bounds.y() + (bounds.height() - canvas.lineHeight()) / 2 + 1,
                canUse ? LABEL_COLOR : DISABLED_LABEL_COLOR);
        canvas.unclip();
        canvas.fill(light, on.getAsBoolean() ? LIGHT_ON : LIGHT_OFF);
        canvas.outline(light, LIGHT_RIM);
        List<Component> lines = tooltip.get();
        if (hovered && !lines.isEmpty()) {
            canvas.tooltip(lines, mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        if (button != MouseButtons.LEFT || !bounds.contains(x, y)) {
            return false;
        }
        if (usable.getAsBoolean()) {
            UiSounds.click();
            turnOver.run();
        }
        return true;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("toggle", label.getString(), bounds, usable.getAsBoolean(),
                Boolean.toString(on.getAsBoolean())));
    }
}
