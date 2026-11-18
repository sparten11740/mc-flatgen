package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockSides;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The one class of Flatgen's that is a game screen: it lays out a screen's {@link Widget}s for its size, draws them
 * through a {@link Canvas} under its title, and hands them the mouse, topmost first. Each Flatgen screen extends it, so
 * when the game's screens change between versions only this class and {@link McCanvas} follow (docs/plan.md 3.3).
 */
public abstract class FlatgenScreen extends Screen {
    /** Where the title's line starts, from the top, in GUI pixels. */
    private static final int TITLE_TOP = 8;
    /** White, the colour of a screen's title as the game draws it. */
    private static final int TITLE_COLOR = 0xFFFFFFFF;

    private final List<Widget> widgets = new ArrayList<>();
    private BlockSides blockSides = new BlockSides();

    protected FlatgenScreen(Component title) {
        super(title);
    }

    /** The screen's widgets, placed for a screen this wide and tall; asked again whenever its size changes. */
    protected abstract List<Widget> layOut(int width, int height);

    @Override
    protected void init() {
        blockSides = new BlockSides();
        widgets.clear();
        widgets.addAll(layOut(width, height));
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
        for (int index = widgets.size() - 1; index >= 0; index--) {
            if (widgets.get(index).click(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (int index = widgets.size() - 1; index >= 0; index--) {
            if (widgets.get(index).scroll(mouseX, mouseY, scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
