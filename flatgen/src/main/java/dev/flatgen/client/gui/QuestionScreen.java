package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * A question before something that cannot be undone, as deleting a preset: a dialog over the screen it asks about,
 * with the picture of what Yes does, the title, the words, and No and Yes. Yes does it and goes back, unless doing it
 * showed another screen; No and Escape go back without.
 */
public final class QuestionScreen extends FlatgenScreen {
    /** How wide the dialog is at most, in GUI pixels. */
    private static final int DIALOG_WIDTH = 320;
    /** Space inside the dialog's edges, and between its parts, in GUI pixels. */
    private static final int PADDING = 14;
    private static final int GAP = 8;
    /** The height of No and Yes, in GUI pixels. */
    private static final int BUTTON_HEIGHT = 19;

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

    /** The dialog in the middle of the screen, as tall as its words need. */
    @Override
    protected List<Widget> layOut(int width, int height) {
        int dialogWidth = Math.min(DIALOG_WIDTH, width - 2 * PADDING);
        List<Component> lines = wrapped(dialogWidth - 2 * PADDING);
        int wordsHeight = TextLines.height(lines.size(), TextStyle.BODY.lineHeight());
        int dialogHeight = 2 * PADDING + Icon.SIZE + TextStyle.TITLE.lineHeight() + wordsHeight + BUTTON_HEIGHT
                + 3 * GAP;
        Rect dialog = new Rect((width - dialogWidth) / 2, (height - dialogHeight) / 2, dialogWidth, dialogHeight);
        int titleTop = dialog.y() + PADDING + Icon.SIZE + GAP;
        int wordsTop = titleTop + TextStyle.TITLE.lineHeight() + GAP;
        int buttonsY = wordsTop + wordsHeight + GAP;
        int yesWidth = actionWidth(CommonComponents.GUI_YES);
        int noWidth = actionWidth(CommonComponents.GUI_NO);
        int yesX = dialog.right() - PADDING - yesWidth;
        Widget answerYes = new TextButton(Icon.line("check"), CommonComponents.GUI_YES, new Rect(yesX, buttonsY,
                yesWidth, BUTTON_HEIGHT), this::answerYes).kind(Look.ButtonKind.PRIMARY);
        no = new TextButton(Icon.line("cross"), CommonComponents.GUI_NO, new Rect(yesX - GAP - noWidth, buttonsY,
                noWidth, BUTTON_HEIGHT), this::onClose);
        return List.of(new Surface(Look.SHEET, dialog),
                new Label(picture, Component.empty(), new Rect(dialog.x() + PADDING, dialog.y() + PADDING, Icon.SIZE,
                        Icon.SIZE)),
                new Caption(this::getTitle, new Rect(dialog.x() + PADDING, titleTop, dialog.width() - 2 * PADDING,
                        TextStyle.TITLE.lineHeight()), TextStyle.TITLE, Look.TEXT),
                new TextLines(new Rect(dialog.x() + PADDING, wordsTop, dialog.width() - 2 * PADDING, wordsHeight),
                        () -> lines),
                new WidgetGroup(List.of(no, answerYes)));
    }

    /** Nothing: the title is in the dialog. */
    @Override
    protected void drawBehind(Canvas canvas) {
    }

    /** The screen the question asks about, dimmed behind the dialog. */
    @Override
    protected Optional<Screen> behind() {
        return Optional.of(parent);
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
        return words.stream().flatMap(line -> wrapped(line, lineWidth).stream()).toList();
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
