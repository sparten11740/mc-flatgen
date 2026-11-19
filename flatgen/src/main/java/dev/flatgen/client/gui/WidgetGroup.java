package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Widgets that act as one, as a screen's or a part of it: drawn in order, handed a click and the wheel topmost first
 * until one takes it, the drag and the release by the one that took the click, and, while the group has the keyboard,
 * the keyboard by the one that took the last click if it takes the keyboard.
 */
public final class WidgetGroup implements Widget {
    private final List<Widget> widgets;
    /** The widget that took the last click; null when none did. */
    private Widget pressed;
    /** The widget that has the keyboard; null when none has. */
    private Widget keyboard;

    public WidgetGroup(List<Widget> widgets) {
        this.widgets = List.copyOf(widgets);
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        for (Widget widget : widgets) {
            widget.draw(canvas, mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        pressed = null;
        for (int index = widgets.size() - 1; index >= 0 && pressed == null; index--) {
            Widget widget = widgets.get(index);
            pressed = widget.click(x, y, button, modifiers) ? widget : null;
        }
        return pressed != null;
    }

    @Override
    public void drag(double x, double y) {
        if (pressed != null) {
            pressed.drag(x, y);
        }
    }

    @Override
    public void release(double x, double y) {
        if (pressed != null) {
            pressed.release(x, y);
        }
    }

    @Override
    public boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        for (int index = widgets.size() - 1; index >= 0; index--) {
            if (widgets.get(index).scroll(x, y, amount, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean takesKeyboard() {
        return pressed != null && pressed.takesKeyboard();
    }

    /**
     * Hands the keyboard to the widget that took the last click while the group has it, telling that widget again even
     * when it had it, so a group within passes it on to the widget within it that took the click.
     */
    @Override
    public void keyboard(boolean held) {
        Widget holder = held && takesKeyboard() ? pressed : null;
        if (keyboard != null && keyboard != holder) {
            keyboard.keyboard(false);
        }
        keyboard = holder;
        if (keyboard != null) {
            keyboard.keyboard(true);
        }
    }

    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return keyboard != null && keyboard.key(keyCode, modifiers);
    }

    @Override
    public boolean type(char character) {
        return keyboard != null && keyboard.type(character);
    }

    /** The narration of the topmost widget at the point that has one. */
    @Override
    public Optional<Narration> narrationAt(double x, double y) {
        for (int index = widgets.size() - 1; index >= 0; index--) {
            Optional<Narration> said = widgets.get(index).narrationAt(x, y);
            if (said.isPresent()) {
                return said;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Widget> focusables() {
        return widgets.stream().flatMap(widget -> widget.focusables().stream()).toList();
    }

    @Override
    public List<WidgetDescription> describe() {
        List<WidgetDescription> descriptions = new ArrayList<>();
        widgets.forEach(widget -> descriptions.addAll(widget.describe()));
        return descriptions;
    }

    /** Passes on to the widget holding the control at that area, so a scrolling group inside scrolls to it. */
    @Override
    public void reveal(Rect area) {
        for (Widget widget : widgets) {
            if (widget.focusables().stream()
                    .anyMatch(control -> control.focusArea().filter(area::equals).isPresent())) {
                widget.reveal(area);
            }
        }
    }
}
