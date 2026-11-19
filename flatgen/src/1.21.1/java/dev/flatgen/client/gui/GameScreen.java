package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.mc.BlockSides;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The game's {@link Screen} as this version calls it, turned into the calls {@link FlatgenScreen} answers alike on
 * every version: drawing through a {@link Canvas}, and the mouse, the wheel, keys and typed characters as plain values.
 * What a call does not take goes on to the game's own handling.
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

    /** Which of Shift, Control (Command on macOS) and Alt are held now. */
    protected static Modifiers modifiers() {
        return new Modifiers(hasShiftDown(), hasControlDown(), hasAltDown());
    }

    @Override
    protected void init() {
        blockSides = new BlockSides();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        McCanvas canvas = new McCanvas(graphics, font, blockSides);
        draw(canvas, mouseX, mouseY);
        canvas.finish();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return click(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return drag(mouseX, mouseY, button) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return release(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scroll(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifierBits) {
        return key(keyCode) || super.keyPressed(keyCode, scanCode, modifierBits);
    }

    @Override
    public boolean charTyped(char character, int modifierBits) {
        return type(character) || super.charTyped(character, modifierBits);
    }
}
