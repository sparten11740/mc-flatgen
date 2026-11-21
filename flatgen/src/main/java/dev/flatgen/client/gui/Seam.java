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
 * The seam between two rows of a list, or under the last, which adds a row there: a faint dashed line across it with a
 * plus under the rows' pictures. The mouse over it or the keyboard's focus turns it into a slim strip in the accent
 * colour saying what it adds; a click, Enter or Space adds the row. It is drawn and used only while shown, so not
 * while a row is dragged across it.
 *
 * @param bounds the seam, across the rows and as tall as the gap between them
 * @param plusX  where its plus is centred, under the rows' pictures
 * @param words  what it adds, such as a layer here, said on its strip
 * @param action what adding does
 * @param shown  whether it is there at all
 * @param usable whether it can be used now
 * @param whyNot why it cannot, shown while it cannot
 */
public record Seam(Rect bounds, int plusX, Component words, Runnable action, BooleanSupplier shown,
        BooleanSupplier usable, Supplier<List<Component>> whyNot) implements Control {
    /** How large the plus is drawn, and the space between it and what is beside it, in GUI pixels. */
    private static final int PLUS = 8;
    private static final int GAP = 3;

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        if (!shown.getAsBoolean()) {
            return;
        }
        boolean canUse = usable.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        Rect plus = new Rect(plusX - PLUS / 2, bounds.y() + (bounds.height() - PLUS) / 2, PLUS, PLUS);
        if (canUse && hovered) {
            drawStrip(canvas, plus);
            return;
        }
        int lineY = bounds.y() + bounds.height() / 2;
        canvas.dashedLine(bounds.x(), lineY, plus.x() - GAP - bounds.x(), Look.GHOST_EDGE);
        canvas.dashedLine(plus.right() + GAP, lineY, bounds.right() - plus.right() - GAP, Look.GHOST_EDGE);
        canvas.sprite(Look.icon("plus"), plus);
        if (!canUse) {
            canvas.dim(plus);
        }
        if (hovered) {
            canvas.tooltipBelow(whyNot.get(), bounds);
        }
    }

    /** The strip it turns into: a lit face with a dashed edge, the plus, and what it adds. */
    private void drawStrip(Canvas canvas, Rect plus) {
        canvas.sprite(Look.GHOST_LIT, bounds);
        canvas.dashedFrame(bounds, Look.ACCENT);
        canvas.sprite(Look.icon("plus"), plus);
        canvas.text(words, plus.right() + GAP, TextStyle.BODY.top(bounds.y(), bounds.height()), Look.ACCENT);
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
