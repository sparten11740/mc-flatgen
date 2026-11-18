package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import dev.flatgen.mc.BlockSides;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
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
    /** White, the colour of a screen's title as the game draws it. */
    private static final int TITLE_COLOR = 0xFFFFFFFF;

    private final List<Widget> widgets = new ArrayList<>();
    private BlockSides blockSides = new BlockSides();
    /** The widget that took the click whose button is held, which hears the drag and the release. */
    private Widget pressed;
    /** The widget that has the keyboard. */
    private Widget keyboard;

    protected FlatgenScreen(Component title) {
        super(title);
    }

    /** The screen's widgets, placed for a screen this wide and tall; asked again whenever its size changes. */
    protected abstract List<Widget> layOut(int width, int height);

    /** A key no widget took: the screen's own shortcuts; whether it was one. */
    protected boolean shortcut(int keyCode, Modifiers modifiers) {
        return false;
    }

    /** What every widget that can be found says of itself, for tools that drive the screen. */
    public List<WidgetDescription> describeWidgets() {
        List<WidgetDescription> descriptions = new ArrayList<>();
        widgets.forEach(widget -> widget.describe().ifPresent(descriptions::add));
        return descriptions;
    }

    @Override
    protected void init() {
        blockSides = new BlockSides();
        widgets.clear();
        widgets.addAll(layOut(width, height));
        pressed = null;
        keyboard = null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        McCanvas canvas = new McCanvas(graphics, font, blockSides);
        canvas.centeredText(title, new Rect(0, TITLE_TOP, width, font.lineHeight), TITLE_COLOR);
        for (Widget widget : widgets) {
            widget.draw(canvas, mouseX, mouseY);
        }
        canvas.finish();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Optional<Widget> taker = Optional.empty();
        for (int index = widgets.size() - 1; index >= 0 && taker.isEmpty(); index--) {
            Widget widget = widgets.get(index);
            taker = widget.click(mouseX, mouseY, button, modifiers()) ? Optional.of(widget) : Optional.empty();
        }
        pressed = taker.orElse(null);
        giveKeyboard(taker.filter(Widget::takesKeyboard).orElse(null));
        return taker.isPresent() || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (pressed != null) {
            pressed.drag(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressed != null) {
            pressed.release(mouseX, mouseY);
            pressed = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (int index = widgets.size() - 1; index >= 0; index--) {
            if (widgets.get(index).scroll(mouseX, mouseY, scrollY, modifiers())) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifierBits) {
        if (keyboard != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            giveKeyboard(null);
            return true;
        }
        if (keyboard != null && keyboard.key(keyCode, modifiers())) {
            return true;
        }
        return shortcut(keyCode, modifiers()) || super.keyPressed(keyCode, scanCode, modifierBits);
    }

    @Override
    public boolean charTyped(char character, int modifierBits) {
        return keyboard != null && keyboard.type(character) || super.charTyped(character, modifierBits);
    }

    private void giveKeyboard(Widget widget) {
        if (keyboard == widget) {
            return;
        }
        if (keyboard != null) {
            keyboard.keyboard(false);
        }
        keyboard = widget;
        if (keyboard != null) {
            keyboard.keyboard(true);
        }
    }

    private static Modifiers modifiers() {
        return new Modifiers(hasShiftDown(), hasControlDown(), hasAltDown());
    }
}
