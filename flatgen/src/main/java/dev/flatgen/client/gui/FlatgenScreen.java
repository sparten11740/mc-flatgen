package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.FocusOrder;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import dev.flatgen.mc.BlockSides;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The one class of Flatgen's that is a game screen: it lays out a screen's {@link Widget}s for its size, draws them
 * through a {@link Canvas} under its title, and hands them the mouse and the keyboard as {@link Widget} describes. Each
 * Flatgen screen extends it, so when the game's screens change between versions only this class and {@link McCanvas}
 * follow (docs/plan.md 3.3).
 */
public abstract class FlatgenScreen extends Screen {
    /** Where the title's line starts, from the top, in GUI pixels. */
    private static final int TITLE_TOP = 8;
    /** How far below the focused control its tooltip is shown, in GUI pixels. */
    private static final int TOOLTIP_GAP = 4;
    /** White, the colour of a screen's title as the game draws it. */
    private static final int TITLE_COLOR = 0xFFFFFFFF;

    private WidgetGroup widgets = new WidgetGroup(List.of());
    /** The screen's widgets as laid out, each an area the keyboard's focus moves between with Tab. */
    private List<Widget> areas = List.of();
    /** The control that has the keyboard's focus, by its place; none before Tab or after a click. */
    private Optional<FocusOrder.Place> focus = Optional.empty();
    /** Where the mouse was when the screen was last drawn, for the narration of what is under it. */
    private int mouseX;
    private int mouseY;
    private BlockSides blockSides = new BlockSides();
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

    /** The control the focus starts on when Tab is first pressed; the first control by default. */
    protected Optional<Widget> firstFocus() {
        return Optional.empty();
    }

    /** A key no widget took: the screen's own shortcuts; whether it was one. */
    protected boolean shortcut(int keyCode, Modifiers modifiers) {
        return false;
    }

    /** What every widget that can be found says of itself, for tools that drive the screen. */
    public List<WidgetDescription> describeWidgets() {
        return widgets.describe();
    }

    @Override
    protected void init() {
        blockSides = new BlockSides();
        areas = layOut(width, height);
        widgets = new WidgetGroup(areas);
        pressed = false;
        keyboard = false;
        focused().ifPresent(control -> control.keyboard(true));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        McCanvas canvas = new McCanvas(graphics, font, blockSides);
        canvas.centeredText(title, new Rect(0, TITLE_TOP, width, font.lineHeight), TITLE_COLOR);
        widgets.draw(canvas, mouseX, mouseY);
        focused().ifPresent(control -> showFocus(canvas, control));
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        canvas.finish();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (pressed) {
            return true;
        }
        moveFocus(Optional.empty());
        pressed = widgets.click(mouseX, mouseY, button, modifiers());
        pressedButton = button;
        keyboard = widgets.takesKeyboard();
        widgets.keyboard(keyboard);
        return pressed || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (pressed) {
            if (button == pressedButton) {
                widgets.drag(mouseX, mouseY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressed) {
            if (button == pressedButton) {
                widgets.release(mouseX, mouseY);
                pressed = false;
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * The wheel, vertical or, as macOS turns Shift with the wheel, horizontal, which then counts the same. As a click
     * does, it ends the keyboard's focus, as what it scrolls may take the focused control out of sight.
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double amount = scrollY != 0 ? scrollY : scrollX;
        moveFocus(Optional.empty());
        return widgets.scroll(mouseX, mouseY, amount, modifiers())
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifierBits) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            boolean forward = !hasShiftDown();
            moveFocus(focus.isEmpty() && forward ? firstPlace() : FocusOrder.tab(counts(), focus, forward));
            return true;
        }
        if (focusKey(keyCode)) {
            return true;
        }
        if (keyboard && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            keyboard = false;
            widgets.keyboard(false);
            return true;
        }
        if (keyboard && widgets.key(keyCode, modifiers())) {
            return true;
        }
        return shortcut(keyCode, modifiers()) || super.keyPressed(keyCode, scanCode, modifierBits);
    }

    @Override
    public boolean charTyped(char character, int modifierBits) {
        Optional<Widget> focused = focused();
        if (focused.isPresent()) {
            return focused.get().type(character) || super.charTyped(character, modifierBits);
        }
        return (keyboard && widgets.type(character)) || super.charTyped(character, modifierBits);
    }

    /** A key for the control that has the focus; an arrow it does not take moves the focus within its area. */
    private boolean focusKey(int keyCode) {
        Optional<Widget> focused = focused();
        if (focused.isEmpty()) {
            return false;
        }
        if (focused.get().key(keyCode, modifiers())) {
            return true;
        }
        Optional<FocusOrder.Place> next = Keys.isArrow(keyCode)
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

    /** Draws the focus ring round a control, and its tooltip just below it. */
    private static void showFocus(Canvas canvas, Widget control) {
        Optional<Rect> area = control.focusArea();
        if (area.isEmpty()) {
            return;
        }
        canvas.focusRing(area.get());
        List<Component> tooltip = control.tooltip();
        if (!tooltip.isEmpty()) {
            canvas.tooltip(tooltip, area.get().x(), area.get().bottom() + TOOLTIP_GAP);
        }
    }

    /** Where the focus starts: on the screen's first focus if it has one, or else on the first control. */
    private Optional<FocusOrder.Place> firstPlace() {
        return firstFocus().flatMap(this::placeOf).or(() -> FocusOrder.tab(counts(), Optional.empty(), true));
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

    private static Modifiers modifiers() {
        return new Modifiers(hasShiftDown(), hasControlDown(), hasAltDown());
    }
}
