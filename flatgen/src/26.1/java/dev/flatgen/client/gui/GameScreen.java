package dev.flatgen.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.mc.BlockSides;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

/**
 * The game's {@link Screen} as this version calls it, with input events and a drawing state to extract into, turned
 * into the calls {@link FlatgenScreen} answers alike on every version: drawing through a {@link Canvas}, and the mouse,
 * the wheel, keys and typed characters as plain values. What a call does not take goes on to the game's own handling.
 */
abstract class GameScreen extends Screen {
    /** The blocks' sides drawn on this screen, looked up afresh each time it is laid out. */
    private BlockSides blockSides = new BlockSides();

    protected GameScreen(Component title) {
        super(title);
    }

    /** Draws the screen's own content over what the game drew. */
    protected abstract void draw(Canvas canvas, int mouseX, int mouseY);

    /** A mouse button pressed here; whether the screen took it. */
    protected abstract boolean click(double x, double y, int button);

    /** The mouse moved here with a button held; whether the screen took it. */
    protected abstract boolean drag(double x, double y, int button);

    /** A mouse button let go here; whether the screen took it. */
    protected abstract boolean release(double x, double y, int button);

    /** The wheel turned here, vertically and horizontally; whether the screen took it. */
    protected abstract boolean scroll(double x, double y, double horizontal, double vertical);

    /** A key pressed, by its GLFW code; whether the screen took it. */
    protected abstract boolean key(int keyCode);

    /** A character typed; whether the screen took it. */
    protected abstract boolean type(char character);

    /** Which of Shift, Control (Command on macOS, as the game counts it) and Alt are held now. */
    protected static Modifiers modifiers() {
        Window window = Minecraft.getInstance().getWindow();
        boolean control = Util.getPlatform() == Util.OS.OSX
                ? held(window, GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER)
                : held(window, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
        return new Modifiers(held(window, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT), control,
                held(window, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT));
    }

    private static boolean held(Window window, int left, int right) {
        return InputConstants.isKeyDown(window, left) || InputConstants.isKeyDown(window, right);
    }

    @Override
    protected void init() {
        blockSides = new BlockSides();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        McCanvas canvas = new McCanvas(graphics, font, blockSides);
        draw(canvas, mouseX, mouseY);
        canvas.finish();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return click(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return drag(event.x(), event.y(), event.button()) || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return release(event.x(), event.y(), event.button()) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scroll(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return key(event.key()) || super.keyPressed(event);
    }

    /** A character typed, one beyond Java's char range given as its two halves. */
    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean taken = false;
        for (char character : Character.toChars(event.codepoint())) {
            taken |= type(character);
        }
        return taken || super.charTyped(event);
    }
}
