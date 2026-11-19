package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * A question before something that cannot be undone, as deleting a preset: its words, and Yes and No. Yes does it and
 * goes back, unless doing it showed another screen; No and Escape go back without.
 */
public final class QuestionScreen extends FlatgenScreen {
    /** Where the picture and the words start, below the title, in GUI pixels. */
    private static final int PICTURE_TOP = 20;
    private static final int WORDS_TOP = 40;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 8;
    /** Space at the sides of the words, in GUI pixels. */
    private static final int MARGIN = 24;

    private final Screen parent;
    private final Icon picture;
    private final List<Component> words;
    private final Runnable yes;
    /** The No button as last laid out, where the keyboard's focus starts, so Enter alone does nothing for good. */
    private Widget no;

    /**
     * Asks a question titled so, in these words, running {@code yes} and going back to {@code parent} on Yes; the
     * picture of what Yes does stands above the words, so a player who cannot read knows what they agree to.
     */
    public QuestionScreen(Screen parent, Icon picture, Component title, List<Component> words, Runnable yes) {
        super(title);
        this.parent = parent;
        this.picture = picture;
        this.words = List.copyOf(words);
        this.yes = yes;
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        List<Component> lines = wrapped(width - 2 * MARGIN);
        int wordsHeight = TextLines.height(lines.size(), font.lineHeight);
        int buttonsY = WORDS_TOP + wordsHeight + 2 * GAP;
        FlowLayout row = new FlowLayout(new Rect((width - 2 * BUTTON_WIDTH - GAP) / 2, buttonsY,
                2 * BUTTON_WIDTH + GAP, 0), BUTTON_HEIGHT, GAP);
        Widget answerYes = new TextButton(Icon.TICK, CommonComponents.GUI_YES, row.next(BUTTON_WIDTH), this::answerYes);
        no = new TextButton(Icon.CROSS, CommonComponents.GUI_NO, row.next(BUTTON_WIDTH), this::onClose);
        return List.of(
                new Label(picture, Component.empty(), new Rect((width - Icon.SIZE) / 2, PICTURE_TOP, Icon.SIZE,
                        Icon.SIZE)),
                new TextLines(new Rect(MARGIN, WORDS_TOP, width - 2 * MARGIN, wordsHeight), () -> lines),
                new WidgetGroup(List.of(answerYes, no)));
    }

    /** The title and the question's words, as the narrator says them when the question opens. */
    @Override
    public Component getNarrationMessage() {
        List<Component> said = new ArrayList<>(List.of(getTitle()));
        said.addAll(words);
        return CommonComponents.joinForNarration(said.toArray(Component[]::new));
    }

    @Override
    protected Optional<Widget> firstFocus() {
        return Optional.ofNullable(no);
    }

    /** The question's words in lines no wider than this. */
    private List<Component> wrapped(int lineWidth) {
        return words.stream().flatMap(line -> font.getSplitter().splitLines(line, lineWidth, Style.EMPTY).stream())
                .<Component>map(part -> Component.literal(part.getString())).toList();
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
