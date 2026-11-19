package dev.flatgen.client.gui;

import com.mojang.blaze3d.platform.Window;
import dev.flatgen.client.gui.rules.LayoutScale;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockSides;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The game's {@link Screen} as this version calls it, turned into the calls {@link FlatgenScreen} answers alike on
 * every version: drawing through a {@link Canvas}, and the mouse, the wheel, keys and typed characters as plain values.
 * What a call does not take goes on to the game's own handling.
 *
 * <p>A Flatgen screen counts in GUI pixels of its own ({@link LayoutScale}): its {@code width} and {@code height}
 * are set in them as it is laid out, it draws scaled from them to the game's, and the mouse comes to it in them.
 */
abstract class GameScreen extends Screen {
    /** The blocks' sides drawn on this screen, looked up afresh each time it is laid out. */
    private BlockSides blockSides = new BlockSides();
    /** How far toward the player a screen standing over another draws, above that one's items. */
    private static final float OVER_BEHIND = 200;
    /** The screen's own GUI pixels for the window as last laid out. */
    private LayoutScale scale = LayoutScale.of(LayoutScale.MIN_WIDTH, LayoutScale.MIN_HEIGHT, 1);

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

    /** How many window pixels one of the screen's GUI pixels is. */
    protected int pixelsPerUnit() {
        return scale.pixelsPerUnit();
    }

    /** Where an area of the screen is in the game's GUI pixels, as tools that drive the game click. */
    public Rect inGameGui(Rect area) {
        return scale.toGame(area);
    }

    /** The screen this one stands over, drawn dimmed behind it, as a picker stands over the editor; none by default. */
    protected Optional<Screen> behind() {
        return Optional.empty();
    }

    /**
     * Sets the screen's size in its own GUI pixels for the window as it is now, before it is laid out, and lays out
     * the screen it stands over for the window too.
     */
    @Override
    protected void init() {
        Window window = Minecraft.getInstance().getWindow();
        scale = LayoutScale.of(window.getWidth(), window.getHeight(), window.getGuiScale());
        width = scale.width();
        height = scale.height();
        behind().ifPresent(screen -> screen.init(Minecraft.getInstance(), window.getGuiScaledWidth(),
                window.getGuiScaledHeight()));
        blockSides = new BlockSides();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.pose().pushPose();
        if (behind().isPresent()) {
            graphics.pose().translate(0, 0, OVER_BEHIND);
        }
        graphics.pose().scale(scale.gameUnit(), scale.gameUnit(), 1);
        McCanvas canvas = new McCanvas(graphics, font, blockSides, scale);
        draw(canvas, (int) scale.fromGame(mouseX), (int) scale.fromGame(mouseY));
        graphics.pose().popPose();
        canvas.finish();
    }

    /**
     * Flatgen's own background, plain, in place of the game's; or the screen this one stands over, dimmed, with what
     * this one draws lifted above that screen's items.
     */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Optional<Screen> under = behind();
        if (under.isEmpty()) {
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), Look.BACKGROUND);
            return;
        }
        under.get().render(graphics, -1, -1, partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, OVER_BEHIND);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), Look.SCRIM);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return click(scale.fromGame(mouseX), scale.fromGame(mouseY), button)
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return drag(scale.fromGame(mouseX), scale.fromGame(mouseY), button)
                || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return release(scale.fromGame(mouseX), scale.fromGame(mouseY), button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scroll(scale.fromGame(mouseX), scale.fromGame(mouseY), scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
