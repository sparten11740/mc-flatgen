package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.biomes.rules.BiomeKinds;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.client.gui.PictureTab;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.WidgetGroup;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.facts.client.FactPictures;
import dev.flatgen.facts.rules.Fact;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.facts.rules.Situation;
import dev.flatgen.mc.Names;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The biome picker (F07), as round 3 of docs/design/simple-editor.md drew it: the dimension's biome now in its header;
 * down its side the starred and the recent biomes, each kind of biome and every biome; the side's choice as landscapes
 * in the middle; and at the right the biome chosen, painted larger, with what it does to the world in words beside
 * their pictures and a button that picks it. A click on a card chooses its biome, a second click picks it.
 */
public final class BiomePickerScreen extends PickerScreen {
    /** Space between the side, the grid and the panel, in GUI pixels. */
    private static final int GAP = 8;
    /** Widths in GUI pixels: the side and the panel at the right. */
    private static final int SIDE_WIDTH = 92;
    private static final int PANEL_WIDTH = 132;
    /** A shelf's row on the side, and the space parting the side's groups, in GUI pixels. */
    private static final int SIDE_ROW = 18;
    private static final int SIDE_SEPARATOR = 6;
    /** The dimension's biome now in the header, painted, in GUI pixels. */
    private static final int NOW_WIDTH = 26;
    /** A card, the landscape on it and the space inside its edge, in GUI pixels. */
    private static final int CARD_WIDTH = 78;
    private static final int CARD_HEIGHT = 52;
    private static final int CARD_PICTURE = 38;
    private static final int INSET = 2;
    /** Space inside the panel, the landscape on it, its Pick button and the space between facts, in GUI pixels. */
    private static final int PANEL_PADDING = 6;
    private static final int PANEL_PICTURE = 60;
    private static final int PICK_HEIGHT = 20;
    private static final int FACT_GAP = 4;
    /** Pictures of the side's choices: the starred, the recent and every biome. */
    private static final Icon STARRED = Icon.item("minecraft:nether_star");
    private static final Icon RECENT = Icon.item("minecraft:clock");
    private static final Icon EVERY = Icon.item("minecraft:map");
    private static final int RIM = 0xFF000000;

    private final Map<String, BiomeInfo> biomes;
    private final Function<BiomeInfo, Situation> situation;
    private final Component now;
    private final String current;
    private final List<Shelf> shelves;
    private final Map<String, List<Fact.Shown>> facts = new HashMap<>();
    /** The shelf the grid shows. */
    private Shelf shelf;
    /** The biome chosen, which the panel shows and its button picks. */
    private String chosen;

    /**
     * What the side lists: the biomes starred, picked lately, of a kind or every one.
     *
     * @param name     its name
     * @param picture  its picture
     * @param of       its biomes among those given, in their order
     * @param newGroup whether a gap parts it from the shelf above
     */
    private record Shelf(Component name, Icon picture, Function<List<Search.Entry>, List<Search.Entry>> of,
            boolean newGroup) {
    }

    /**
     * A picker of these biomes going back to {@code parent}, handing a picked biome's id to {@code onPick}.
     *
     * @param biomes    every biome of the world being created, by id
     * @param situation the world as the editor has it, but of a biome, for its facts
     * @param now       what the header says of the dimension's biome now, as "The Overworld's biome now"
     * @param current   that biome, by id, the one chosen at first unless the game lacks it
     */
    BiomePickerScreen(Screen parent, Picker picker, Map<String, BiomeInfo> biomes,
            Function<BiomeInfo, Situation> situation, Component now, String current, Consumer<String> onPick) {
        super(parent, picker, onPick);
        this.biomes = biomes;
        this.situation = situation;
        this.now = now;
        this.current = current;
        this.chosen = biomes.containsKey(current) ? current : entries().get(0).id();
        this.shelves = shelves();
        this.shelf = shelves.get(2);
    }

    /** The starred and the recent biomes, then each kind of biome that has any, the last of them every biome. */
    private List<Shelf> shelves() {
        List<Shelf> made = new ArrayList<>(List.of(
                new Shelf(Component.translatable("flatgen.picks.starred"), STARRED,
                        shown -> picked(shown, picks().starred()), false),
                new Shelf(Component.translatable("flatgen.picks.recent"), RECENT,
                        shown -> picked(shown, picks().recent()), false)));
        BiomeKinds.Kind every = BiomeKinds.ALL.get(BiomeKinds.ALL.size() - 1);
        for (BiomeKinds.Kind kind : BiomeKinds.ALL) {
            Function<List<Search.Entry>, List<Search.Entry>> of = shown -> ofKind(shown, kind);
            List<Search.Entry> any = of.apply(entries());
            if (!any.isEmpty()) {
                made.add(new Shelf(Component.translatable(kind.name()),
                        kind == every ? EVERY : new BiomeIcon(biomes.get(any.get(0).id())), of,
                        made.size() == 2 || kind == every));
            }
        }
        return made;
    }

    /** The biomes shown whose ids are among these, in these ids' order. */
    private static List<Search.Entry> picked(List<Search.Entry> shown, List<String> ids) {
        return ids.stream().flatMap(id -> shown.stream().filter(entry -> entry.id().equals(id)).limit(1)).toList();
    }

    private List<Search.Entry> ofKind(List<Search.Entry> shown, BiomeKinds.Kind kind) {
        return shown.stream().filter(entry -> kind.holds().test(biomes.get(entry.id()))).toList();
    }

    /** The dimension's biome now: painted, what the header says of it above its name. */
    @Override
    protected List<Widget> header(Rect area) {
        Rect painted = new Rect(area.x(), area.y(), NOW_WIDTH, area.height());
        int wordsX = painted.right() + GAP;
        int line = TextStyle.BODY.lineHeight();
        int top = area.y() + (area.height() - 2 * line) / 2;
        return List.of(new Painted(() -> biomes.get(current), painted),
                new Caption(() -> now, new Rect(wordsX, top, area.right() - wordsX, line), TextStyle.BODY,
                        Look.MUTED),
                new Caption(() -> Names.biome(current), new Rect(wordsX, top + line, area.right() - wordsX, line),
                        TextStyle.STRONG, Look.TEXT));
    }

    /** The side at the left, the panel at the right and the grid between. */
    @Override
    protected List<Widget> body(Rect area) {
        Rect panel = new Rect(area.right() - PANEL_WIDTH, area.y(), PANEL_WIDTH, area.height());
        int gridX = area.x() + SIDE_WIDTH + GAP;
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new WidgetGroup(side(new Rect(area.x(), area.y(), SIDE_WIDTH, area.height()))));
        widgets.add(grid(new Rect(gridX, area.y(), panel.x() - GAP - gridX, area.height()), CARD_WIDTH, CARD_HEIGHT,
                new Cards(), this::choose));
        widgets.addAll(panel(panel));
        return widgets;
    }

    /** The shelf's biomes under its name, or what a search found among every biome shown. */
    @Override
    protected List<CardGrid.Section> sections(List<Search.Entry> shown, String searched) {
        return List.of(searched.isBlank() ? new CardGrid.Section(shelf.name(), shelf.of().apply(shown))
                : new CardGrid.Section(Component.translatable("flatgen.picks.found"), Search.find(shown, searched)));
    }

    /** A row for each shelf, gaps parting its groups. */
    private List<Widget> side(Rect area) {
        List<Widget> rows = new ArrayList<>();
        int y = area.y();
        for (Shelf each : shelves) {
            y += each.newGroup() ? SIDE_SEPARATOR : 0;
            rows.add(new PictureTab(each.picture(), each::name, new Rect(area.x(), y, area.width(), SIDE_ROW),
                    () -> shelf == each, () -> show(each), () -> List.of(each.name())));
            y += SIDE_ROW;
        }
        return rows;
    }

    /** The biome chosen: painted, its name and its mod, its facts in words, and the button that picks it. */
    private List<Widget> panel(Rect panel) {
        Rect inside = new Rect(panel.x() + PANEL_PADDING, panel.y() + PANEL_PADDING,
                panel.width() - 2 * PANEL_PADDING, panel.height() - 2 * PANEL_PADDING);
        Rect picture = new Rect(inside.x(), inside.y(), inside.width(), PANEL_PICTURE);
        Rect name = new Rect(inside.x(), picture.bottom() + PANEL_PADDING, inside.width(),
                TextStyle.TITLE.lineHeight());
        Rect from = new Rect(inside.x(), name.bottom(), inside.width(), TextStyle.BODY.lineHeight());
        Rect pick = new Rect(inside.x(), inside.bottom() - PICK_HEIGHT, inside.width(), PICK_HEIGHT);
        int factsTop = from.bottom() + PANEL_PADDING;
        return List.of(new Surface(Look.PANEL, panel), new Painted(() -> biomes.get(chosen), picture),
                new Caption(() -> Names.biome(chosen), name, TextStyle.TITLE, Look.TEXT),
                new Caption(() -> Component.literal(modName(biomes.get(chosen).namespace())), from, TextStyle.BODY,
                        Look.MUTED),
                new FactList(new Rect(inside.x(), factsTop, inside.width(), pick.y() - PANEL_PADDING - factsTop)),
                new TextButton(Icon.line("check"),
                        () -> Component.translatable("flatgen.biomes.pick", Names.biome(chosen)), pick,
                        () -> pick(chosen), () -> true, List::of).kind(Look.ButtonKind.PRIMARY));
    }

    /** Shows a shelf's biomes, from the grid's top. */
    private void show(Shelf shown) {
        shelf = shown;
        showAnew();
    }

    /** Chooses a biome, or picks it when it is chosen already. */
    private void choose(String id) {
        if (id.equals(chosen)) {
            pick(id);
        } else {
            chosen = id;
        }
    }

    /** A biome's facts in the world as the editor has it, worked out once per biome while the picker is open. */
    private List<Fact.Shown> facts(String id) {
        return facts.computeIfAbsent(id, biome -> show(situation.apply(biomes.get(biome))));
    }

    private static List<Fact.Shown> show(Situation world) {
        return Facts.ABOUT_THE_BIOME.stream().map(fact -> fact.show(world)).toList();
    }

    /** A biome's card: its landscape over its name, ringed while it is the one chosen; its facts in its tooltip. */
    private final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            boolean isChosen = entry.id().equals(chosen);
            Rect picture = new Rect(place.x() + INSET, place.y() + INSET, place.width() - 2 * INSET, CARD_PICTURE);
            BiomeLandscape.draw(canvas, biomes.get(entry.id()), picture);
            Component name = canvas.fitted(Component.literal(entry.name()), picture.width(), TextStyle.BODY);
            canvas.text(name, picture.x(), TextStyle.BODY.top(picture.bottom(), place.bottom() - picture.bottom()),
                    isChosen ? Look.TEXT : Look.MUTED);
            if (isChosen) {
                canvas.sprite(Look.CHOSEN_RING, place);
            }
        }

        @Override
        public List<Component> describe(Search.Entry entry) {
            return facts(entry.id()).stream().flatMap(shown -> FactPictures.words(shown).stream()).toList();
        }
    }

    /** A biome painted over an area, as the supplier has it now, framed. */
    private static final class Painted implements Widget {
        private final Supplier<BiomeInfo> biome;
        private final Rect area;

        Painted(Supplier<BiomeInfo> biome, Rect area) {
            this.biome = biome;
            this.area = area;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            Optional.ofNullable(biome.get()).ifPresent(shown -> BiomeLandscape.draw(canvas, shown, area));
            canvas.outline(area, RIM);
        }
    }

    /** The chosen biome's facts, each its picture beside its words, wrapped to the panel. */
    private final class FactList implements Widget {
        private final Rect area;

        FactList(Rect area) {
            this.area = area;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            int y = area.y();
            int wordsX = area.x() + Icon.SIZE + 2 * INSET;
            for (Fact.Shown shown : facts(chosen)) {
                FactPictures.icon(shown).draw(canvas, area.x(), y);
                List<Component> lines = FactPictures.words(shown).stream()
                        .flatMap(words -> wrapped(words, area.right() - wordsX).stream()).toList();
                drawLines(canvas, lines, wordsX, y);
                y += Math.max(Icon.SIZE, lines.size() * TextStyle.BODY.lineHeight()) + FACT_GAP;
            }
        }

        /** Lines of words beside a picture whose top is here, in the middle beside it when they are fewer. */
        private void drawLines(Canvas canvas, List<Component> lines, int x, int top) {
            int y = top + Math.max(0, (Icon.SIZE - lines.size() * TextStyle.BODY.lineHeight()) / 2);
            for (Component line : lines) {
                canvas.text(line, x, y, Look.TEXT);
                y += TextStyle.BODY.lineHeight();
            }
        }
    }
}
