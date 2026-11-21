package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * The seam between two rows of a list, or under the last, which adds a row there: a faint dashed line across most of
 * it with a plus and a slab in its middle. The mouse over it turns the line the accent colour and shows the hand; a
 * click, Enter or Space adds the row, and the narrator says what it adds. It is drawn and used only while shown, so not while a row is dragged across it.
 *
 * @param bounds the seam, across the rows and as tall as the gap between them
 * @param words  what it adds, such as a layer here, for the narrator
 * @param action what adding does
 * @param shown  whether it is there at all
 * @param usable whether it can be used now
 * @param whyNot why it cannot, shown while it cannot
 */
public record Seam(Rect bounds, Component words, Runnable action, BooleanSupplier shown, BooleanSupplier usable,
        Supplier<List<Component>> whyNot) implements Control {
    /** How large the plus and the slab are drawn, and the space around them, in GUI pixels. */
    private static final int PICTURE = 8;
    private static final int GAP = 1;
    /** How much of the seam's width its dashed line spans, centred, in hundredths. */
    private static final int LINE_SHARE = 90;

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        if (!shown.getAsBoolean()) {
            return;
        }
        boolean canUse = usable.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        int lineColor = canUse && hovered ? Look.ACCENT : Look.GHOST_EDGE;
        int lineWidth = bounds.width() * LINE_SHARE / 100;
        int left = bounds.x() + (bounds.width() - lineWidth) / 2;
        int lineY = bounds.y() + bounds.height() / 2;
        int pictures = 2 * PICTURE + GAP;
        Rect plus = new Rect(bounds.x() + (bounds.width() - pictures) / 2, lineY - PICTURE / 2, PICTURE, PICTURE);
        Rect slab = new Rect(plus.right() + GAP, plus.y(), PICTURE, PICTURE);
        canvas.dashedLine(left, lineY, plus.x() - GAP - left, lineColor);
        canvas.dashedLine(slab.right() + GAP, lineY, left + lineWidth - slab.right() - GAP, lineColor);
        canvas.sprite(Look.icon("plus"), plus);
        canvas.sprite(Look.icon("slab"), slab);
        if (!canUse) {
            canvas.dim(new Rect(plus.x(), plus.y(), pictures, PICTURE));
        }
        if (!hovered) {
            return;
        }
        if (canUse) {
            canvas.showHand();
        } else {
            canvas.tooltipBelow(whyNot.get(), bounds);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return shown.getAsBoolean() && button == MouseButtons.LEFT && bounds.contains(x, y)
                && Control.use(usable, action);
    }

    /** Enter or Space adds the row while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Keys.uses(keyCode) && Control.use(usable, action);
    }

    @Override
    public List<Widget> focusables() {
        return shown.getAsBoolean() ? List.of(this) : List.of();
    }

    @Override
    public List<Component> tooltip() {
        return usable.getAsBoolean() ? List.of() : whyNot.get();
    }

    @Override
    public Optional<Narration> narration() {
        List<Component> lines = new ArrayList<>(List.of(words));
        lines.addAll(tooltip());
        return Optional.of(Narration.button(lines, usable.getAsBoolean()));
    }

    @Override
    public List<WidgetDescription> describe() {
        return shown.getAsBoolean()
                ? List.of(new WidgetDescription("button", words.getString(), bounds, usable.getAsBoolean(), ""))
                : List.of();
    }
}
