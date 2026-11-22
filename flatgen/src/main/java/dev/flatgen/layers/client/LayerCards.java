package dev.flatgen.layers.client;

import dev.flatgen.blocks.client.BlockChoice;
import dev.flatgen.blocks.client.BlockMarks;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.GhostRow;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.NumberField;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.Stepper;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.ListSlots;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.rules.Edit;
import dev.flatgen.layers.rules.LayerEdits;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Names;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

/**
 * The layers as cards, top first (F29): each dragged to another place by its grip or any part of it that is no control,
 * with its block with how many blocks thick the layer is, which opens the block picker, its name, thinner and thicker,
 * and remove; in Advanced also its Ys and id, its thickness to type and a copy. The chosen layer's card is outlined.
 * Every control works on its own card's layer, which it selects first, so the column and the cards always show the same
 * layer chosen. Above the cards, a row not there yet adds a layer on top, asking for the block first.
 */
final class LayerCards {
    /** A card's height and the space between cards, in GUI pixels. */
    private static final int CARD_HEIGHT = 36;
    private static final int CARD_GAP = 4;
    /** Space inside a card at its sides, in GUI pixels. */
    private static final int PADDING = 6;
    /** The width of the grip at a card's left, which shows where a card is taken to drag it, in GUI pixels. */
    private static final int GRIP_WIDTH = 18;
    /** The block's tile, and the picture of the block on it, in GUI pixels. */
    private static final int TILE = 28;
    private static final int PICTURE = 22;
    /**
     * A card's buttons, and the field to type a thickness, in GUI pixels: even, as the cards are, so that every picture
     * sits exactly in the middle.
     */
    private static final int BUTTON = 18;
    private static final int FIELD = 30;
    /** Space between a card's controls, in GUI pixels. */
    private static final int SPACING = 4;
    /** The count's pill on the block's tile: its height and the space at each side of the number, in GUI pixels. */
    private static final int COUNT_HEIGHT = 10;
    private static final int COUNT_PADDING = 3;
    /** A square picturing one block of a layer's thickness, and the space between squares, in GUI pixels. */
    private static final int SQUARE = 7;
    private static final int SQUARE_GAP = 2;
    /** The most blocks shown one square each; a thicker layer shows this many less one, folded, then one more. */
    private static final int MOST_SQUARES = 5;
    /** How far the mouse moves with its button held on a card before the card lifts, in GUI pixels; less is a click. */
    private static final int LIFT_AFTER = 3;
    /** How far below and right of a lifted card its shadow falls, in GUI pixels, and the shadow's colour, ARGB. */
    private static final int SHADOW_OFFSET = 2;
    private static final int SHADOW = 0x80000000;

    private LayerCards() {
    }

    /**
     * The cards in this area, scrolling when they do not fit, scrolled to show the chosen layer's card and kept so as
     * the chosen layer changes, by a click, a key or an edit.
     */
    static List<Widget> list(EditorContext editor, Rect area) {
        Rect newRow = new Rect(area.x(), area.y(), area.width(), CARD_HEIGHT);
        int cardsTop = newRow.bottom() + CARD_GAP;
        Rect cardsArea = new Rect(area.x(), cardsTop, area.width(), area.bottom() - cardsTop);
        Drag drag = new Drag();
        CopyPlace copy = new CopyPlace();
        ScrollGroup cards = new ScrollGroup(cardsArea, top -> layOut(editor, cardsArea, top, drag, copy));
        chosen(editor, cardsArea).ifPresent(cards::revealContent);
        return List.of(newRow(editor, newRow), cards, new FollowChosen(editor, cardsArea, cards, drag),
                new FollowCopy(editor, cardsArea, cards, drag, copy));
    }

    /** Opens the block picker, then adds a layer of the block picked at a place, as one step to undo. */
    static void insert(EditorContext editor, int at) {
        BlockChoice.choose(editor, block -> editor.apply(new LayerEdits.Insert(block, at)));
    }

    /** The row above the cards that adds a layer on top. */
    private static GhostRow newRow(EditorContext editor, Rect row) {
        Rect tile = tile(row);
        return new GhostRow(row, tile, picture(tile), tile.right() + 2 * SPACING,
                Component.translatable("flatgen.editor.new_layer"),
                () -> insert(editor, editor.shown().layers().size()), () -> roomLeft(editor) > 0,
                () -> LayerText.whyFull(editor.shown(), editor.world()));
    }

    /** The square at a card's left holding its block's picture. */
    private static Rect tile(Rect card) {
        return new Rect(card.x() + GRIP_WIDTH, card.y() + (card.height() - TILE) / 2, TILE, TILE);
    }

    /** Where a block's picture is drawn on its tile: in its middle, as the tile's picture button draws it. */
    private static Rect picture(Rect tile) {
        return new Rect(tile.x() + (TILE - PICTURE) / 2, tile.y() + (TILE - PICTURE) / 2, PICTURE, PICTURE);
    }

    /**
     * Every layer's card, top first, in this area with the first card's top here, as the list scrolls, the copy a
     * card's Copy would make in a place of its own below that card, and the card lifted while one is dragged.
     */
    private static ScrollGroup.Laid layOut(EditorContext editor, Rect area, int top, Drag drag, CopyPlace copy) {
        Rect from = new Rect(area.x(), top, area.width(), 0);
        ListSlots slots = slots(editor, from);
        int count = slots.count();
        boolean copying = copy.layer != CopyPlace.NONE;
        int copyPlace = copying ? count - copy.layer : count;
        ListSlots shown = new ListSlots(from, copying ? count + 1 : count, CARD_HEIGHT, CARD_GAP);
        copy.buttons.clear();
        List<Widget> widgets = new ArrayList<>();
        for (int place = 0; place < count; place++) {
            Rect card = shown.at(place < copyPlace ? place : place + 1);
            widgets.addAll(card(editor, count - 1 - place, card, slots, drag, copy.buttons::add));
        }
        copy.place = copying ? Optional.of(shown.at(copyPlace)) : Optional.empty();
        copy.place.ifPresent(place -> widgets.add(new CopyCard(editor, copy.layer, place)));
        widgets.add(new LiftedCard(editor, slots, drag));
        return new ScrollGroup.Laid(widgets, shown.bottom());
    }

    /** The cards' slots, the top layer's first, from this area's top. */
    private static ListSlots slots(EditorContext editor, Rect area) {
        return new ListSlots(area, editor.shown().layers().size(), CARD_HEIGHT, CARD_GAP);
    }

    /** Where the chosen layer's card is with the list at its top, if a layer is chosen. */
    private static Optional<Rect> chosen(EditorContext editor, Rect area) {
        ListSlots slots = slots(editor, area);
        int selected = editor.shown().selected();
        return selected < 0 ? Optional.empty() : Optional.of(slots.at(slots.count() - 1 - selected));
    }

    /**
     * One layer's card: its background, which selects it and drags it where no control is, and its controls, in the
     * order they stand from the left, which the keyboard follows: the block, the thickness, Copy in Advanced, Remove.
     */
    private static List<Widget> card(EditorContext editor, int index, Rect card, ListSlots slots, Drag drag,
            Consumer<Copyable> copies) {
        int middle = card.y() + (card.height() - BUTTON) / 2;
        Rect remove = new Rect(card.right() - PADDING - BUTTON, middle, BUTTON, BUTTON);
        Optional<Rect> copy = editor.advanced()
                ? Optional.of(new Rect(remove.x() - SPACING - BUTTON, middle, BUTTON, BUTTON)) : Optional.empty();
        int stepperRight = copy.orElse(remove).x() - SPACING;
        int stepperLeft = stepperRight - 2 * BUTTON - (editor.advanced() ? FIELD : 0);
        Rect tile = tile(card);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new PictureButton(blockPicture(editor, index), tile, () -> pick(editor, index),
                () -> Stream.of(Names.block(block(editor, index)), BlockChoice.hints(editor, index),
                        Component.translatable("flatgen.editor.pick_block"))
                        .filter(line -> !line.getString().isEmpty()).toList()).onFocus(() -> editor.select(index)));
        widgets.addAll(thickness(editor, index, stepperLeft, middle));
        copy.ifPresent(place -> widgets.add(copyButton(editor, index, place, copies)));
        widgets.add(button(editor, index, Icon.line("trash"), "flatgen.editor.remove", remove, new LayerEdits.Remove(),
                () -> true, List::of));
        int nameX = tile.right() + 2 * SPACING;
        widgets.addAll(words(editor, index, new Rect(nameX, card.y(), stepperLeft - SPACING - nameX, card.height())));
        List<Rect> controls = widgets.stream().flatMap(widget -> widget.focusArea().stream()).toList();
        widgets.add(0, new Background(editor, index, card, controls, slots, drag));
        return widgets;
    }

    /** A layer's thinner and thicker, with its thickness to type between them in Advanced, from this left edge. */
    private static List<Widget> thickness(EditorContext editor, int index, int left, int middle) {
        Rect thinner = new Rect(left, middle, BUTTON, BUTTON);
        Optional<NumberField> field = editor.advanced()
                ? Optional.of(new NumberField("height", Component.translatable("flatgen.layer.thickness_field").copy()
                        .append(" · ").append(Names.block(block(editor, index))),
                        new Rect(thinner.right(), middle, FIELD, BUTTON), () -> height(editor, index),
                        height -> act(editor, index, new LayerEdits.SetHeight(height)))
                        .onFocus(() -> editor.select(index)))
                : Optional.empty();
        Rect thicker = new Rect(thinner.right() + field.map(number -> FIELD).orElse(0), middle, BUTTON, BUTTON);
        return Stepper.of(button(editor, index, Icon.line("minus"), "flatgen.layer.thinner", thinner,
                new LayerEdits.ChangeHeight(-1), () -> height(editor, index) > 1, List::of), field,
                button(editor, index, Icon.line("plus"), "flatgen.layer.thicker", thicker,
                        new LayerEdits.ChangeHeight(1), () -> roomLeft(editor) > 0,
                        () -> LayerText.whyFull(editor.shown(), editor.world())));
    }

    /** Copy, in Advanced, whose copy is shown below the card while the mouse or the keyboard's focus is on it. */
    private static PictureButton copyButton(EditorContext editor, int index, Rect place, Consumer<Copyable> copies) {
        PictureButton copy = button(editor, index, Icon.line("copy"), "flatgen.editor.duplicate", place,
                new LayerEdits.Duplicate(), () -> roomLeft(editor) > 0,
                () -> LayerText.whyFull(editor.shown(), editor.world()));
        copies.accept(new Copyable(index, copy));
        return copy;
    }

    /** The layer's block's name, and below it squares picturing its thickness; in Advanced, its Ys and its id. */
    private static List<Widget> words(EditorContext editor, int index, Rect area) {
        if (!editor.advanced()) {
            int both = TextStyle.STRONG.lineHeight() + SQUARE_GAP + SQUARE;
            Rect name = new Rect(area.x(), area.y() + (area.height() - both) / 2, area.width(),
                    TextStyle.STRONG.lineHeight());
            return List.of(new Caption(() -> Names.block(block(editor, index)), name, TextStyle.STRONG, Look.TEXT),
                    new Thickness(editor, index, new Rect(area.x(), name.bottom() + SQUARE_GAP, area.width(),
                            SQUARE)));
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
     * One of a card's picture buttons: its picture, the edit it makes on the card's layer, and its tooltip: its name,
     * the layer's block, so the narrator tells the cards apart, then why it cannot be used.
     */
    private static PictureButton button(EditorContext editor, int index, Icon icon, String name, Rect place,
            Edit edit, BooleanSupplier usable, Supplier<List<Component>> why) {
        return new PictureButton(icon, place, () -> act(editor, index, edit), usable,
                () -> Stream.concat(Stream.of(Component.translatable(name), Names.block(block(editor, index))),
                        why.get().stream()).toList()).onFocus(() -> editor.select(index));
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
        Rect picture = new Rect(x, y, PICTURE, PICTURE);
        canvas.blockIcon(block(editor, index), picture);
        BlockMarks.draw(canvas, block(editor, index), picture);
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
     * A card's drag, which the cards, the card lifted under the mouse and the list's following share: whether the
     * mouse's button is held on a card, whether a card is dragged, how far below its top it was taken, and where the
     * mouse is.
     */
    private static final class Drag {
        private boolean held;
        private boolean active;
        private int heldAt;
        private int mouseY;
    }

    /**
     * A card's background, outlined while its layer is chosen, showing the hand wherever no control of the card is: a
     * press there selects the layer, and moving the mouse on drags the layer to another place, the card lifted under
     * the mouse and the place it would land left empty, until let go there.
     */
    private static final class Background implements Widget {
        private final EditorContext editor;
        private final int index;
        private final Rect card;
        /** Where the card's controls are, which take the mouse's presses themselves. */
        private final List<Rect> controls;
        /** The cards' slots as laid out with this one, which a drag finds its place among. */
        private final ListSlots slots;
        private final Drag drag;
        /** The height the mouse's button went down at on this card, in GUI pixels. */
        private int pressedAt;
        private boolean dragging;

        Background(EditorContext editor, int index, Rect card, List<Rect> controls, ListSlots slots, Drag drag) {
            this.editor = editor;
            this.index = index;
            this.card = card;
            this.controls = controls;
            this.slots = slots;
            this.drag = drag;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            boolean chosen = editor.shown().selected() == index;
            canvas.sprite(chosen ? Look.CHOSEN : Look.PANEL, card);
            canvas.sprite(Look.icon("grip"), new Rect(card.x() + (GRIP_WIDTH - Icon.SIZE) / 2,
                    card.y() + (card.height() - Icon.SIZE) / 2, Icon.SIZE, Icon.SIZE));
            if (free(mouseX, mouseY)) {
                canvas.showHand();
            }
        }

        @Override
        public boolean click(double x, double y, int button, Modifiers modifiers) {
            if (button != MouseButtons.LEFT || !free(x, y)) {
                return false;
            }
            editor.select(index);
            drag.held = true;
            pressedAt = (int) y;
            return true;
        }

        /** Lifts the card once the mouse has moved far enough with its button held, then moves it with the mouse. */
        @Override
        public void drag(double x, double y) {
            if (drag.held && !dragging && Math.abs(y - pressedAt) >= LIFT_AFTER) {
                dragging = true;
                drag.active = true;
                drag.heldAt = pressedAt - card.y();
            }
            if (dragging) {
                drag.mouseY = (int) y;
                editor.preview(new LayerEdits.Move(slots.count() - 1 - slots.placeAt(y)));
            }
        }

        @Override
        public void release(double x, double y) {
            drag.held = false;
            if (dragging) {
                editor.commitPreview();
                dragging = false;
                drag.active = false;
            }
        }

        /** Whether a point is on the card where none of its controls is. */
        private boolean free(double x, double y) {
            return card.contains(x, y) && controls.stream().noneMatch(control -> control.contains(x, y));
        }
    }

    /**
     * A card's Copy button.
     *
     * @param index  the card's layer
     * @param button the button
     */
    private record Copyable(int index, PictureButton button) {
    }

    /** Which card's copy has a place of its own in the list, and the cards' Copy buttons, as last laid out. */
    private static final class CopyPlace {
        /** No card's copy is shown. */
        private static final int NONE = -1;
        /** The layer whose copy is shown, or {@link #NONE}. */
        private int layer = NONE;
        /** Where the copy is shown. */
        private Optional<Rect> place = Optional.empty();
        private final List<Copyable> buttons = new ArrayList<>();
    }

    /**
     * The copy a card's Copy would make, a faint card with the accent colour's dashed edge in its place below the card,
     * its block and its name.
     */
    private record CopyCard(EditorContext editor, int index, Rect card) implements Widget {
        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            canvas.sprite(Look.GHOST_LIT, card);
            canvas.dashedFrame(card, Look.ACCENT);
            Rect picture = picture(tile(card));
            drawBlock(canvas, editor, index, picture.x(), picture.y());
            canvas.text(Names.block(block(editor, index)), tile(card).right() + 2 * SPACING,
                    TextStyle.STRONG.top(card.y(), card.height()), Look.MUTED, TextStyle.STRONG);
        }
    }

    /**
     * Gives a card's copy its place below the card while the mouse or the keyboard's focus is on the card's Copy, and
     * keeps it while the mouse is on the copy, so the card stays under the mouse and only the cards below it move. The
     * cards are laid out again as the copy comes and goes, but not while a card is held or a field typed in, whose
     * widgets laid out again would not hear the rest.
     */
    private static final class FollowCopy implements Widget {
        private final EditorContext editor;
        private final Rect area;
        private final ScrollGroup cards;
        private final Drag drag;
        private final CopyPlace copy;

        FollowCopy(EditorContext editor, Rect area, ScrollGroup cards, Drag drag, CopyPlace copy) {
            this.editor = editor;
            this.area = area;
            this.cards = cards;
            this.drag = drag;
            this.copy = copy;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            if (drag.held || cards.holdsKeyboard()) {
                return;
            }
            int wanted = wanted(mouseX, mouseY);
            if (wanted != copy.layer) {
                int focused = copy.buttons.stream().filter(copyable -> copyable.button().focused())
                        .mapToInt(Copyable::index).findFirst().orElse(CopyPlace.NONE);
                copy.layer = wanted;
                cards.layOutAgain();
                copy.buttons.stream().filter(copyable -> copyable.index() == focused)
                        .forEach(copyable -> copyable.button().keyboard(true));
            }
        }

        /**
         * The layer whose copy is wanted: the one whose Copy the mouse is on in the cards' window, or the focus is on,
         * and the one shown while the mouse is on its copy; none while no layer has room.
         */
        private int wanted(int mouseX, int mouseY) {
            if (roomLeft(editor) < 1) {
                return CopyPlace.NONE;
            }
            boolean inside = area.contains(mouseX, mouseY);
            if (inside && copy.place.filter(place -> place.contains(mouseX, mouseY)).isPresent()) {
                return copy.layer;
            }
            return copy.buttons.stream()
                    .filter(copyable -> (inside && copyable.button().bounds().contains(mouseX, mouseY))
                            || copyable.button().focused())
                    .mapToInt(Copyable::index).findFirst().orElse(CopyPlace.NONE);
        }
    }

    /**
     * While a card is dragged, that card lifted under the mouse over the others, with a shadow, and the place it would
     * land left empty and ringed; nothing otherwise. The lifted card is drawn by the card's own widgets.
     */
    private static final class LiftedCard implements Widget {
        private final EditorContext editor;
        private final ListSlots slots;
        private final Drag drag;

        LiftedCard(EditorContext editor, ListSlots slots, Drag drag) {
            this.editor = editor;
            this.slots = slots;
            this.drag = drag;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            int index = editor.shown().selected();
            if (!drag.active || index < 0) {
                return;
            }
            canvas.showHand();
            Rect landing = slots.at(slots.count() - 1 - index);
            int top = Math.max(slots.at(0).y(), Math.min(slots.at(slots.count() - 1).y(), drag.mouseY - drag.heldAt));
            Rect lifted = new Rect(landing.x(), top, landing.width(), landing.height());
            canvas.raised(() -> drawLifted(canvas, index, landing, lifted));
        }

        private void drawLifted(Canvas canvas, int index, Rect landing, Rect lifted) {
            canvas.fill(landing, Look.BACKGROUND);
            canvas.sprite(Look.CHOSEN_RING, landing);
            canvas.fill(new Rect(lifted.x() + SHADOW_OFFSET, lifted.y() + SHADOW_OFFSET, lifted.width(),
                    lifted.height()), SHADOW);
            card(editor, index, lifted, slots, new Drag(), copyable -> { })
                    .forEach(widget -> widget.draw(canvas, -1, -1));
        }
    }

    /**
     * A layer's thickness pictured under its name, for a player who does not read numbers: a square of its block for
     * each block, or for a thick layer a few squares, a fold and one more square.
     */
    private static final class Thickness implements Widget {
        private final EditorContext editor;
        private final int index;
        private final Rect area;
        /** The biome {@link #water} is the colour of, the colour a water layer's squares are tinted. */
        private String tintedBiome = "";
        private int water;

        Thickness(EditorContext editor, int index, Rect area) {
            this.editor = editor;
            this.index = index;
            this.area = area;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            int blocks = height(editor, index);
            if (blocks == 0) {
                return;
            }
            boolean folded = blocks > MOST_SQUARES;
            int before = folded ? MOST_SQUARES - 1 : blocks;
            int x = area.x();
            for (int square = 0; square < before; square++) {
                drawSquare(canvas, x);
                x += SQUARE + SQUARE_GAP;
            }
            if (folded) {
                canvas.sprite(Look.icon("fold"), new Rect(x, area.y() + (SQUARE - Icon.SIZE) / 2, Icon.SIZE,
                        Icon.SIZE));
                drawSquare(canvas, x + Icon.SIZE + SQUARE_GAP);
            }
        }

        private void drawSquare(Canvas canvas, int x) {
            canvas.blockSide(block(editor, index), waterColor(), new Rect(x, area.y(), SQUARE, SQUARE));
        }

        /** The water colour of the biome shown, looked up again only when the biome changes. */
        private int waterColor() {
            String biome = editor.shown().biome();
            if (!biome.equals(tintedBiome)) {
                tintedBiome = biome;
                water = Biomes.waterColor(biome, editor.registries());
            }
            return water;
        }
    }

    /**
     * Keeps the chosen layer's card in sight: when the chosen layer changes, by a click, the arrow keys or an edit, the
     * list scrolls to its card, though not while the mouse's button is held on a card, as scrolling lays the cards out
     * anew and the card held would never hear the button let go; it follows once it is. It draws nothing of its own.
     */
    private static final class FollowChosen implements Widget {
        private final EditorContext editor;
        private final Rect area;
        private final ScrollGroup cards;
        private final Drag drag;
        private int followed;

        FollowChosen(EditorContext editor, Rect area, ScrollGroup cards, Drag drag) {
            this.editor = editor;
            this.area = area;
            this.cards = cards;
            this.drag = drag;
            this.followed = editor.shown().selected();
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            int selected = editor.shown().selected();
            if (selected != followed && !drag.held) {
                followed = selected;
                chosen(editor, area).ifPresent(cards::revealContent);
            }
        }
    }
}
