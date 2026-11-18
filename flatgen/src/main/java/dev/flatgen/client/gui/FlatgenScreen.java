package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import dev.flatgen.mc.BlockSides;
import java.util.List;
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

    private WidgetGroup widgets = new WidgetGroup(List.of());
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
        widgets = new WidgetGroup(layOut(width, height));
        pressed = false;
        keyboard = false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        McCanvas canvas = new McCanvas(graphics, font, blockSides);
        canvas.centeredText(title, new Rect(0, TITLE_TOP, width, font.lineHeight), TITLE_COLOR);
        widgets.draw(canvas, mouseX, mouseY);
        canvas.finish();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (pressed) {
            return true;
        }
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

    /** The wheel, vertical or, as macOS turns Shift with the wheel, horizontal, which then counts the same. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double amount = scrollY != 0 ? scrollY : scrollX;
        return widgets.scroll(mouseX, mouseY, amount, modifiers())
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifierBits) {
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
        return (keyboard && widgets.type(character)) || super.charTyped(character, modifierBits);
    }

    private static Modifiers modifiers() {
        return new Modifiers(hasShiftDown(), hasControlDown(), hasAltDown());
    }
}
