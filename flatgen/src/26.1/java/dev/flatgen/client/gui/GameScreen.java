package dev.flatgen.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.flatgen.client.gui.rules.LayoutScale;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockSides;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * The game's {@link Screen} as this version calls it, with input events and a drawing state to extract into, turned
 * into the calls {@link FlatgenScreen} answers alike on every version: drawing through a {@link Canvas}, and the mouse,
 * the wheel, keys and typed characters as plain values. What a call does not take goes on to the game's own handling.
 *
 * <p>A Flatgen screen counts in GUI pixels of its own ({@link LayoutScale}): its {@code width} and {@code height}
 * are set in them as it is laid out, it draws scaled from them to the game's, and the mouse comes to it in them.
 */
abstract class GameScreen extends Screen {
    /** The blocks' sides drawn on this screen, looked up afresh each time it is laid out. */
    private BlockSides blockSides = new BlockSides();
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

    /** A key pressed, by its key code ({@code InputConstants.KEY_...}); whether the screen took it. */
    protected abstract boolean key(int keyCode);

    /** A character typed; whether the screen took it. */
    protected abstract boolean type(char character);

    /** The event's mouse button as Flatgen numbers them ({@link MouseButtons}): 26.3 numbers the game's otherwise. */
    private static int button(MouseButtonEvent event) {
        int button = event.button();
        return button == InputConstants.MOUSE_BUTTON_LEFT ? MouseButtons.LEFT
                : button == InputConstants.MOUSE_BUTTON_RIGHT ? MouseButtons.RIGHT : MouseButtons.OTHER;
    }

    /**
     * Which of Shift, Control and Alt are held now, Control being Command on macOS, as the game's edit shortcuts count
     * it.
     */
    protected static Modifiers modifiers() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean control = InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY ? commandDown() : minecraft.hasControlDown();
        return new Modifiers(minecraft.hasShiftDown(), control, minecraft.hasAltDown());
    }

    /** Whether either Command key, the game's Super keys, is held. */
    private static boolean commandDown() {
        //? if >=26.3 {
        /*return InputConstants.isKeyDown(InputConstants.KEY_LGUI) || InputConstants.isKeyDown(InputConstants.KEY_RGUI);
        *///?} else {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSUPER)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSUPER);
        //?}
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
        behind().ifPresent(screen -> screen.init(window.getGuiScaledWidth(), window.getGuiScaledHeight()));
        blockSides = new BlockSides();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale.gameUnit());
        McCanvas canvas = new McCanvas(graphics, font, blockSides, scale);
        draw(canvas, (int) scale.fromGame(mouseX), (int) scale.fromGame(mouseY));
        canvas.finish();
        graphics.pose().popMatrix();
    }

    /**
     * Flatgen's own background, plain, in place of the game's; or the screen this one stands over, dimmed, in a layer
     * below what this one draws so that screen's items stay behind.
     */
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Optional<Screen> under = behind();
        if (under.isEmpty()) {
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), Look.BACKGROUND);
            return;
        }
        under.get().extractBackground(graphics, -1, -1, partialTick);
        under.get().extractRenderState(graphics, -1, -1, partialTick);
        graphics.nextStratum();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), Look.SCRIM);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return click(scale.fromGame(event.x()), scale.fromGame(event.y()), button(event))
                || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return drag(scale.fromGame(event.x()), scale.fromGame(event.y()), button(event))
                || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return release(scale.fromGame(event.x()), scale.fromGame(event.y()), button(event))
                || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scroll(scale.fromGame(mouseX), scale.fromGame(mouseY), scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
