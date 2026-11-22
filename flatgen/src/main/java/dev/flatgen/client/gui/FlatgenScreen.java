package dev.flatgen.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.flatgen.client.gui.rules.FocusOrder;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.PageFrame;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * The base of every Flatgen screen: it lays out a screen's {@link Widget}s for its size, draws them through a
 * {@link Canvas} under its title, and hands them the mouse and the keyboard as {@link Widget} describes. The game's
 * screen reaches it through {@link GameScreen}, one class per version tree, so when the game's screens change between
 * versions only that class and {@code McCanvas} follow (docs/plan.md 3.3).
 */
public abstract class FlatgenScreen extends GameScreen {

    private WidgetGroup widgets = new WidgetGroup(List.of());
    /** The screen's widgets as laid out, each an area the keyboard's focus moves between with Tab. */
    private List<Widget> areas = List.of();
    /** The control that has the keyboard's focus, by its place; none before Tab or after a click. */
    private Optional<FocusOrder.Place> focus = Optional.empty();
    /** Where the mouse was when the screen was last drawn, for the narration of what is under it. */
    private int mouseX;
    private int mouseY;
    /** Whether a widget took the click whose button is held, which then hears the drag and the release. */
    private boolean pressed;
    /** The button held for {@link #pressed}; the others are ignored until it is let go. */
    private int pressedButton;
    /** Whether a widget has the keyboard. */
    private boolean keyboard;

    protected FlatgenScreen(Component title) {
        super(title);
    }

    /** The screen's widgets, placed for a screen this wide and tall; asked again whenever its size changes. */
    protected abstract List<Widget> layOut(int width, int height);

    /** Files the player dropped on the game's window while this screen is open; nothing by default. */
    protected void filesDropped(List<Path> files) {
    }

    /** Draws what is behind the widgets: by default the title at the left of the header, and the frame's lines. */
    protected void drawBehind(Canvas canvas) {
        canvas.text(title, PageFrame.MARGIN, TextStyle.TITLE.top(0, PageFrame.HEADER), Look.TEXT, TextStyle.TITLE);
        drawFrameLines(canvas);
    }

    /** Draws the lines under the header and over the footer of the screen's {@link PageFrame}. */
    protected final void drawFrameLines(Canvas canvas) {
        canvas.fill(new Rect(0, PageFrame.HEADER, width, 1), Look.DIVIDER);
        canvas.fill(new Rect(0, height - PageFrame.FOOTER, width, 1), Look.DIVIDER);
    }

    /** Back, at the left of the header, going back as Escape does. */
    protected final PictureButton backButton() {
        return new PictureButton(Icon.BACK, new Rect(PageFrame.MARGIN, (PageFrame.HEADER - PictureButton.SIZE) / 2,
                PictureButton.SIZE, PictureButton.SIZE), this::onClose, () -> List.of(CommonComponents.GUI_BACK));
    }

    /** The control the focus starts on when Tab is first pressed; the first control by default. */
    protected Optional<Widget> firstFocus() {
        return Optional.empty();
    }

    /** A key no widget took: the screen's own shortcuts; whether it was one. */
    protected boolean shortcut(int keyCode, Modifiers modifiers) {
        return false;
    }

    /** How wide body text is drawn on this screen. */
    protected int textWidth(Component text) {
        return textWidth(text, TextStyle.BODY);
    }

    /** How wide text in a style is drawn on this screen. */
    protected int textWidth(Component text, TextStyle style) {
        return font.width(McCanvas.styled(text, style, pixelsPerUnit()));
    }

    /** How wide one of the footer's actions with a picture and these words is: wide enough for both, never narrow. */
    protected int actionWidth(Component label) {
        return Math.max(PageFrame.MIN_ACTION_WIDTH, TextButton.width(textWidth(label)));
    }

    /** Body text in lines no wider than this, as this screen draws it. */
    protected List<Component> wrapped(Component text, int lineWidth) {
        return font.getSplitter().splitLines(text, lineWidth, McCanvas.style(TextStyle.BODY, pixelsPerUnit())).stream()
                .<Component>map(part -> Component.literal(part.getString())).toList();
    }

    /**
     * The words the screen hands the game's narrator now, for the control with the focus or else the widget under the
     * mouse, for tools that check them.
     */
    public List<String> narratedNow() {
        List<String> words = new ArrayList<>();
        updateNarratedWidget(new NarrationWords(words));
        return words;
    }

    /** What every widget that can be found says of itself, placed in the game's GUI pixels for tools that click. */
    public List<WidgetDescription> describeWidgets() {
        return widgets.describe().stream().map(widget -> new WidgetDescription(widget.kind(), widget.label(),
                inGameGui(widget.bounds()), widget.active(), widget.value())).toList();
    }

    @Override
    protected void init() {
        super.init();
        areas = layOut(width, height);
        widgets = new WidgetGroup(areas);
        pressed = false;
        keyboard = false;
        focused().ifPresent(control -> control.keyboard(true));
    }

    @Override
    protected void draw(Canvas canvas, int mouseX, int mouseY) {
        drawBehind(canvas);
        Optional<Widget> focused = focused();
        Optional<Rect> ring = focused.flatMap(Widget::focusArea);
        ring.ifPresent(area -> canvas.tooltipBelow(focused.get().tooltip(), area));
        widgets.draw(canvas, mouseX, mouseY);
        ring.ifPresent(canvas::focusRing);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    protected boolean click(double x, double y, int button) {
        if (pressed) {
            return true;
        }
        moveFocus(Optional.empty());
        endTypingElsewhere(x, y);
        pressed = widgets.click(x, y, button, modifiers());
        pressedButton = button;
        keyboard = widgets.takesKeyboard();
        widgets.keyboard(keyboard);
        return pressed;
    }

    /**
     * Ends the typing an earlier click began, unless this click is on the widget typed in, so a field is done, as a
     * name saved, before what the click does reads it.
     */
    private void endTypingElsewhere(double x, double y) {
        if (keyboard && widgets.keyboardArea().filter(area -> area.contains(x, y)).isEmpty()) {
            keyboard = false;
            widgets.keyboard(false);
        }
    }

    @Override
    protected boolean drag(double x, double y, int button) {
        if (pressed && button == pressedButton) {
            widgets.drag(x, y);
        }
        return pressed;
    }

    @Override
    protected boolean release(double x, double y, int button) {
        if (pressed && button == pressedButton) {
            widgets.release(x, y);
            pressed = false;
            return true;
        }
        return pressed;
    }

    /**
     * The wheel, vertical or, as macOS turns Shift with the wheel, horizontal, which then counts the same. As a click
     * does, it ends the keyboard's focus, as what it scrolls may take the focused control out of sight; a field that
     * has the keyboard from a click keeps it. While a button is held it does nothing, as scrolling lays widgets out
     * again and the widget dragging would never hear the release.
     */
    @Override
    protected boolean scroll(double x, double y, double horizontal, double vertical) {
        if (pressed) {
            return true;
        }
        if (focus.isPresent()) {
            moveFocus(Optional.empty());
        }
        return widgets.scroll(x, y, vertical != 0 ? vertical : horizontal, modifiers());
    }

    /**
     * Tells the game's narrator about the control that has the focus, or else the widget under the mouse, in the
     * words that widget gives, as the game does for its own widgets.
     */
    @Override
    protected void updateNarratedWidget(NarrationElementOutput output) {
        Optional<Widget> focused = focused();
        Optional<Narration> said = focused.isPresent() ? focused.get().narration()
                : widgets.narrationAt(mouseX, mouseY);
        said.ifPresent(narration -> narration.tell(output.nest(), focused.isPresent()));
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        filesDropped(files);
    }

    @Override
    protected boolean key(int keyCode) {
        if (keyCode == InputConstants.KEY_TAB) {
            boolean forward = !modifiers().shift();
            moveFocus(focus.isEmpty() ? firstPlace(forward) : FocusOrder.tab(counts(), focus, forward));
            return true;
        }
        if (focusKey(keyCode)) {
            return true;
        }
        if (keyboard && keyCode == InputConstants.KEY_ESCAPE) {
            keyboard = false;
            widgets.keyboard(false);
            return true;
        }
        if (keyboard && widgets.key(keyCode, modifiers())) {
            return true;
        }
        return shortcut(keyCode, modifiers());
    }

    @Override
    protected boolean type(char character) {
        Optional<Widget> focused = focused();
        if (focused.isPresent()) {
            return focused.get().type(character);
        }
        return keyboard && widgets.type(character);
    }

    /**
     * A key for the control that has the focus; an arrow it does not take moves the focus within its area, unless Alt
     * is held, which leaves the arrow to the screen's shortcuts, such as moving a layer.
     */
    private boolean focusKey(int keyCode) {
        Optional<Widget> focused = focused();
        if (focused.isEmpty()) {
            return false;
        }
        if (focused.get().key(keyCode, modifiers())) {
            return true;
        }
        Optional<FocusOrder.Place> next = Keys.isArrow(keyCode) && !modifiers().alt()
                ? focus.flatMap(place -> FocusOrder.step(counts(), place, Keys.forward(keyCode))) : Optional.empty();
        next.ifPresent(place -> moveFocus(next));
        return next.isPresent();
    }

    /**
     * Gives the focus to the control at a place, or to none: the control that had it, and one that had the keyboard
     * from a click, let go of the keyboard, and the new one takes it, scrolled into view.
     */
    private void moveFocus(Optional<FocusOrder.Place> to) {
        focused().ifPresent(control -> control.keyboard(false));
        keyboard = false;
        widgets.keyboard(false);
        focus = to;
        focus.ifPresent(place -> focused().flatMap(Widget::focusArea).ifPresent(areas.get(place.area())::reveal));
        focused().ifPresent(control -> control.keyboard(true));
    }

    /**
     * Where the focus starts, by Tab or Shift with Tab: on the screen's first focus if it has one, or else on the first
     * area's first control, or the last area's going back.
     */
    private Optional<FocusOrder.Place> firstPlace(boolean forward) {
        return firstFocus().flatMap(this::placeOf).or(() -> FocusOrder.tab(counts(), Optional.empty(), forward));
    }

    /** A control's place among the areas' controls; none when it is not one of them. */
    private Optional<FocusOrder.Place> placeOf(Widget control) {
        for (int area = 0; area < areas.size(); area++) {
            int index = areas.get(area).focusables().indexOf(control);
            if (index >= 0) {
                return Optional.of(new FocusOrder.Place(area, index));
            }
        }
        return Optional.empty();
    }

    /** How many controls each area has now. */
    private List<Integer> counts() {
        return areas.stream().map(area -> area.focusables().size()).toList();
    }

    /** The control that has the focus, as the areas are laid out now. */
    private Optional<Widget> focused() {
        return focus.flatMap(place -> FocusOrder.within(counts(), place))
                .map(place -> areas.get(place.area()).focusables().get(place.control()));
    }
}
