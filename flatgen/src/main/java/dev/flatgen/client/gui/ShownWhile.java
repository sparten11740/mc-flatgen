package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.network.chat.Component;

/**
 * A widget shown only while a condition holds, keeping its place: hidden, it draws nothing and takes neither the mouse,
 * the keyboard nor the focus, so what is around it never moves as it comes and goes.
 *
 * @param shown  whether it shows now
 * @param widget the widget
 */
public record ShownWhile(BooleanSupplier shown, Widget widget) implements Widget {
    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        if (shown.getAsBoolean()) {
            widget.draw(canvas, mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return shown.getAsBoolean() && widget.click(x, y, button, modifiers);
    }

    @Override
    public void drag(double x, double y) {
        widget.drag(x, y);
    }

    @Override
    public void release(double x, double y) {
        widget.release(x, y);
    }

    @Override
    public boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        return shown.getAsBoolean() && widget.scroll(x, y, amount, modifiers);
    }

    @Override
    public boolean takesKeyboard() {
        return shown.getAsBoolean() && widget.takesKeyboard();
    }

    @Override
    public void keyboard(boolean held) {
        widget.keyboard(held);
    }

    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return shown.getAsBoolean() && widget.key(keyCode, modifiers);
    }

    @Override
    public boolean type(char character) {
        return shown.getAsBoolean() && widget.type(character);
    }

    @Override
    public List<Widget> focusables() {
        return shown.getAsBoolean() ? widget.focusables() : List.of();
    }

    @Override
    public Optional<Rect> focusArea() {
        return shown.getAsBoolean() ? widget.focusArea() : Optional.empty();
    }

    @Override
    public List<Component> tooltip() {
        return shown.getAsBoolean() ? widget.tooltip() : List.of();
    }

    @Override
    public Optional<Narration> narration() {
        return shown.getAsBoolean() ? widget.narration() : Optional.empty();
    }

    @Override
    public Optional<Narration> narrationAt(double x, double y) {
        return shown.getAsBoolean() ? widget.narrationAt(x, y) : Optional.empty();
    }

    @Override
    public void reveal(Rect area) {
        widget.reveal(area);
    }

    @Override
    public List<WidgetDescription> describe() {
        return shown.getAsBoolean() ? widget.describe() : List.of();
    }
}
