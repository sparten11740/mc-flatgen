package dev.flatgen.layers.client;

import dev.flatgen.blocks.client.BlockChoice;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.NumberField;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.rules.Edit;
import dev.flatgen.layers.rules.LayerEdits;
import dev.flatgen.mc.Names;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

/**
 * The layers as cards, top first (F29): each with a grip to drag it to another place, its block with how many blocks
 * thick the layer is, which opens the block picker, its name, thinner and thicker, and remove; in Advanced also its Ys
 * and id, its thickness to type and a copy. The chosen layer's card is outlined. Every control works on its own card's
 * layer, which it selects first, so the column and the cards always show the same layer chosen.
 */
final class LayerCards {
    /** A card's height and the space between cards, in GUI pixels. */
    private static final int CARD_HEIGHT = 35;
    private static final int CARD_GAP = 4;
    /** Space inside a card at its sides, in GUI pixels. */
    private static final int PADDING = 6;
    /** The width of the grip at a card's left, which a drag starts on, in GUI pixels. */
    private static final int GRIP_WIDTH = 18;
    /** The block's tile, and the picture of the block on it, in GUI pixels. */
    private static final int TILE = 27;
    private static final int PICTURE = 22;
    /** A card's buttons, and the field to type a thickness, in GUI pixels. */
    private static final int BUTTON = 17;
    private static final int FIELD = 26;
    /** Space between a card's controls, in GUI pixels. */
    private static final int SPACING = 4;
    /** The count's pill on the block's tile: its height and the space at each side of the number, in GUI pixels. */
    private static final int COUNT_HEIGHT = 10;
    private static final int COUNT_PADDING = 3;

    private LayerCards() {
    }

    /** Every layer's card, top first, in this area with the first card's top here, as the list scrolls. */
    static ScrollGroup.Laid layOut(EditorContext editor, Rect area, int top) {
        int count = editor.shown().layers().size();
        List<Widget> widgets = new ArrayList<>();
        for (int place = 0; place < count; place++) {
            Rect card = new Rect(area.x(), top + place * (CARD_HEIGHT + CARD_GAP), area.width(), CARD_HEIGHT);
            widgets.addAll(card(editor, count - 1 - place, card, top));
        }
        return new ScrollGroup.Laid(widgets, top + count * (CARD_HEIGHT + CARD_GAP) - CARD_GAP);
    }

    /** Where the chosen layer's card is with the list at its top, if a layer is chosen. */
    static Optional<Rect> chosen(EditorContext editor, Rect area) {
        int count = editor.shown().layers().size();
        int selected = editor.shown().selected();
        return selected < 0 ? Optional.empty() : Optional.of(new Rect(area.x(),
                area.y() + (count - 1 - selected) * (CARD_HEIGHT + CARD_GAP), area.width(), CARD_HEIGHT));
    }

    /** One layer's card: its background, which selects it and is dragged by its grip, and its controls. */
    private static List<Widget> card(EditorContext editor, int index, Rect card, int listTop) {
        int middle = card.y() + (card.height() - BUTTON) / 2;
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new Background(editor, index, card, listTop));
        Rect tile = new Rect(card.x() + GRIP_WIDTH, card.y() + (card.height() - TILE) / 2, TILE, TILE);
        widgets.add(new PictureButton(blockPicture(editor, index), tile, () -> pick(editor, index),
                () -> List.of(Names.block(block(editor, index)), BlockChoice.hints(editor, index),
                        Component.translatable("flatgen.editor.pick_block"))));
        int right = card.right() - PADDING;
        Rect remove = new Rect(right - BUTTON, middle, BUTTON, BUTTON);
        widgets.add(button(editor, index, "trash", "flatgen.editor.remove", remove, new LayerEdits.Remove(),
                () -> true, List::of));
        right = remove.x() - SPACING;
        if (editor.advanced()) {
            Rect copy = new Rect(right - BUTTON, middle, BUTTON, BUTTON);
            widgets.add(button(editor, index, "copy", "flatgen.editor.duplicate", copy, new LayerEdits.Duplicate(),
                    () -> roomLeft(editor) > 0, () -> LayerText.whyFull(editor.shown(), editor.world())));
            right = copy.x() - SPACING;
        }
        Rect thicker = new Rect(right - BUTTON, middle, BUTTON, BUTTON);
        widgets.add(button(editor, index, "plus", "flatgen.layer.deeper", thicker, new LayerEdits.ChangeHeight(1),
                () -> roomLeft(editor) > 0, () -> LayerText.whyFull(editor.shown(), editor.world())));
        right = thicker.x();
        if (editor.advanced()) {
            Rect field = new Rect(right - SPACING - FIELD, middle, FIELD, BUTTON);
            widgets.add(new NumberField("height", Component.translatable("flatgen.layer.depth_field"), field,
                    () -> height(editor, index), height -> act(editor, index, new LayerEdits.SetHeight(height))));
            right = field.x();
        }
        Rect thinner = new Rect(right - SPACING - BUTTON, middle, BUTTON, BUTTON);
        widgets.add(button(editor, index, "minus", "flatgen.layer.shallower", thinner,
                new LayerEdits.ChangeHeight(-1), () -> height(editor, index) > 1, List::of));
        int nameX = tile.right() + 2 * SPACING;
        widgets.addAll(words(editor, index, new Rect(nameX, card.y(), thinner.x() - SPACING - nameX, card.height())));
        return widgets;
    }

    /** The layer's block's name; in Advanced, below it, its Ys and its id. */
    private static List<Widget> words(EditorContext editor, int index, Rect area) {
        if (!editor.advanced()) {
            return List.of(new Caption(() -> Names.block(block(editor, index)), area, TextStyle.STRONG, Look.TEXT));
        }
        int lines = 2 * TextStyle.STRONG.lineHeight();
        Rect name = new Rect(area.x(), area.y() + (area.height() - lines) / 2, area.width(), lines / 2);
        Rect detail = new Rect(area.x(), name.bottom(), area.width(), lines / 2);
        return List.of(new Caption(() -> Names.block(block(editor, index)), name, TextStyle.STRONG, Look.TEXT),
                new Caption(() -> detail(editor, index), detail, TextStyle.BODY, Look.MUTED));
    }

    /** A layer's Ys and its block's id. */
    private static Component detail(EditorContext editor, int index) {
        return Component.empty().append(LayerText.yRange(editor.shown(), editor.world(), index))
                .append(" · " + block(editor, index));
    }

    /**
     * One of a card's picture buttons: its picture, the edit it makes on the card's layer, and its tooltip, its name
     * and then why it cannot be used.
     */
    private static PictureButton button(EditorContext editor, int index, String icon, String name, Rect place,
            Edit edit, BooleanSupplier usable, Supplier<List<Component>> why) {
        return new PictureButton(Icon.line(icon), place, () -> act(editor, index, edit), usable,
                () -> Stream.concat(Stream.of(Component.translatable(name)), why.get().stream()).toList());
    }

    /** The layer's block, filling the tile, with how many blocks thick the layer is on a pill at its corner. */
    private static Icon blockPicture(EditorContext editor, int index) {
        return (canvas, x, y) -> drawBlock(canvas, editor, index, x + (Icon.SIZE - PICTURE) / 2,
                y + (Icon.SIZE - PICTURE) / 2);
    }

    private static void drawBlock(Canvas canvas, EditorContext editor, int index, int x, int y) {
        if (index >= editor.shown().layers().size()) {
            return;
        }
        canvas.blockIcon(block(editor, index), new Rect(x, y, PICTURE, PICTURE));
        Component count = Component.literal(Integer.toString(height(editor, index)));
        int width = Math.max(COUNT_HEIGHT, canvas.textWidth(count, TextStyle.STRONG) + 2 * COUNT_PADDING);
        Rect pill = new Rect(x + PICTURE - width + COUNT_PADDING, y + PICTURE - COUNT_HEIGHT + COUNT_PADDING, width,
                COUNT_HEIGHT);
        canvas.count(count, pill);
    }

    /** Selects the card's layer, then makes the edit on it. */
    private static void act(EditorContext editor, int index, Edit edit) {
        editor.select(index);
        editor.apply(edit);
    }

    /** Selects the card's layer and opens the block picker for it. */
    private static void pick(EditorContext editor, int index) {
        editor.select(index);
        BlockChoice.pick(editor);
    }

    private static String block(EditorContext editor, int index) {
        return editor.shown().layers().get(index).block();
    }

    private static int height(EditorContext editor, int index) {
        return index < editor.shown().layers().size() ? editor.shown().layers().get(index).height() : 0;
    }

    private static int roomLeft(EditorContext editor) {
        return LayerText.roomLeft(editor.shown(), editor.world());
    }

    /**
     * A card's background, outlined while its layer is chosen: a click selects the layer, and a press on the grip at
     * its left drags the layer, the cards showing it at the place it would land, until let go there.
     */
    private static final class Background implements Widget {
        private final EditorContext editor;
        private final int index;
        private final Rect card;
        private final int listTop;
        private boolean dragging;

        Background(EditorContext editor, int index, Rect card, int listTop) {
            this.editor = editor;
            this.index = index;
            this.card = card;
            this.listTop = listTop;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            boolean chosen = editor.shown().selected() == index;
            canvas.sprite(chosen ? Look.CHOSEN : Look.PANEL, card);
            canvas.sprite(Look.icon("grip"), new Rect(card.x() + (GRIP_WIDTH - Icon.SIZE) / 2,
                    card.y() + (card.height() - Icon.SIZE) / 2, Icon.SIZE, Icon.SIZE));
        }

        @Override
        public boolean click(double x, double y, int button, Modifiers modifiers) {
            if (button != MouseButtons.LEFT || !card.contains(x, y)) {
                return false;
            }
            editor.select(index);
            dragging = x < card.x() + GRIP_WIDTH;
            return true;
        }

        @Override
        public void drag(double x, double y) {
            if (dragging) {
                editor.preview(new LayerEdits.Move(placeAt(y)));
            }
        }

        @Override
        public void release(double x, double y) {
            if (dragging) {
                editor.commitPreview();
                dragging = false;
            }
        }

        /** The layer whose card is at this height of the list, the top or bottom one beyond the list's ends. */
        private int placeAt(double y) {
            int count = editor.shown().layers().size();
            int place = (int) Math.floor((y - listTop) / (CARD_HEIGHT + CARD_GAP));
            return count - 1 - Math.max(0, Math.min(count - 1, place));
        }
    }
}
