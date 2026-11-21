package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A row of a list that is not there yet, drawn where it would go and as large as the rows: a dashed edge round a faint
 * face, the placeholder block with a plus where a row's picture is, and words where a row's name is. The mouse over it
 * lights it in the accent colour, and the keyboard's focus rings it as every control; a click, Enter or Space adds the
 * row. Its words say what it does, so it has a tooltip only for why it cannot be used.
 *
 * @param bounds  where it is
 * @param tile    the square holding its picture, where a row's picture button is
 * @param picture where its placeholder block is drawn, as large as a row's picture
 * @param textX   where its words start, where a row's name does
 * @param words   what it adds, such as a new layer on top
 * @param action  what adding it does
 * @param usable  whether it can be used now
 * @param whyNot  why it cannot, shown while it cannot
 */
public record GhostRow(Rect bounds, Rect tile, Rect picture, int textX, Component words, Runnable action,
        BooleanSupplier usable, Supplier<List<Component>> whyNot) implements Control {
    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean canUse = usable.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        boolean lit = canUse && hovered;
        canvas.sprite(lit ? Look.GHOST_LIT : Look.GHOST, bounds);
        canvas.dashedFrame(bounds, lit ? Look.ACCENT : Look.GHOST_EDGE);
        canvas.button(tile, lit, canUse);
        canvas.sprite(Look.PLACEHOLDER_BLOCK, picture);
        Icon.line("plus").draw(canvas, picture.x() + (picture.width() - Icon.SIZE) / 2,
                picture.y() + (picture.height() - Icon.SIZE) / 2);
        if (!canUse) {
            canvas.dim(tile);
        }
        canvas.text(words, textX, TextStyle.STRONG.top(bounds.y(), bounds.height()), canUse ? Look.TEXT : Look.DISABLED,
                TextStyle.STRONG);
        if (hovered && !canUse) {
            canvas.tooltipBelow(whyNot.get(), bounds);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return Control.clicked(bounds, x, y, button, usable, action);
    }

    /** Enter or Space adds the row while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Control.keyed(keyCode, usable, action);
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
        return List.of(new WidgetDescription("button", words.getString(), bounds, usable.getAsBoolean(), ""));
    }
}
