package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * A question before something that cannot be undone, as deleting a preset: its words, and Yes and No. Yes does it and
 * goes back, unless doing it showed another screen; No and Escape go back without.
 */
public final class QuestionScreen extends FlatgenScreen {
    /** Where the words start, below the title, in GUI pixels. */
    private static final int WORDS_TOP = 40;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 8;
    /** Space at the sides of the words, in GUI pixels. */
    private static final int MARGIN = 24;

    private final Screen parent;
    private final List<Component> words;
    private final Runnable yes;

    /** Asks a question titled so, in these words, running {@code yes} and going back to {@code parent} on Yes. */
    public QuestionScreen(Screen parent, Component title, List<Component> words, Runnable yes) {
        super(title);
        this.parent = parent;
        this.words = List.copyOf(words);
        this.yes = yes;
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        int wordsHeight = words.size() * (font.lineHeight + 3);
        int buttonsY = WORDS_TOP + wordsHeight + 2 * GAP;
        FlowLayout row = new FlowLayout(new Rect((width - 2 * BUTTON_WIDTH - GAP) / 2, buttonsY,
                2 * BUTTON_WIDTH + GAP, 0), BUTTON_HEIGHT, GAP);
        return List.of(
                new TextLines(new Rect(MARGIN, WORDS_TOP, width - 2 * MARGIN, wordsHeight), () -> words),
                new TextButton(CommonComponents.GUI_YES, row.next(BUTTON_WIDTH), this::answerYes),
                new TextButton(CommonComponents.GUI_NO, row.next(BUTTON_WIDTH), this::onClose));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** Does what was asked, then goes back, unless doing it already showed another screen. */
    private void answerYes() {
        yes.run();
        if (minecraft.screen == this) {
            onClose();
        }
    }
}
