package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.biomes.rules.BiomeKinds;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.ShelvedPickerScreen;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The biome picker (F07), as round 3 of docs/design/simple-editor.md drew it: the dimension's biome now in its header;
 * down its side the starred and the recent biomes, each kind of biome and every biome; the side's choice as landscapes
 * in the middle; and at the right the biome looked at, painted larger, with what it does to the world in words beside
 * their pictures: the one the mouse or the keyboard is on, else the biome now. A click on a card picks its biome.
 */
public final class BiomePickerScreen extends ShelvedPickerScreen {
    /** Space between the grid and the panel, and in the header, in GUI pixels. */
    private static final int GAP = 8;
    /** The panel's width at the right, in GUI pixels. */
    private static final int PANEL_WIDTH = 132;
    /** The dimension's biome now in the header, painted, in GUI pixels. */
    private static final int NOW_WIDTH = 26;
    /** A card, the landscape on it and the space inside its edge, in GUI pixels. */
    private static final int CARD_WIDTH = 78;
    private static final int CARD_HEIGHT = 52;
    private static final int CARD_PICTURE = 38;
    private static final int INSET = 2;
    /** Space inside the panel, the landscape on it and the space between facts, in GUI pixels. */
    private static final int PANEL_PADDING = 6;
    private static final int PANEL_PICTURE = 60;
    private static final int FACT_GAP = 4;
    /** The biomes Every biome's picture shows, four in a square, of those the game has. */
    private static final List<String> EVERY = List.of("minecraft:plains", "minecraft:desert",
            "minecraft:snowy_plains", "minecraft:ocean");

    private final Map<String, BiomeInfo> biomes;
    private final Function<BiomeInfo, Situation> situation;
    private final Component now;
    private final String current;
    private final Map<String, List<Fact.Shown>> facts = new HashMap<>();
    /** The biome the panel shows while none is looked at: the biome now, or the first when the game lacks it. */
    private final String idle;

    /**
     * A picker of these biomes going back to {@code parent}, handing a picked biome's id to {@code onPick}.
     *
     * @param biomes    every biome of the world being created, by id
     * @param situation the world as the editor has it, but of a biome, for its facts
     * @param now       what the header says of the dimension's biome now, as "The Overworld's biome now"
     * @param current   that biome, by id, ringed among the cards
     */
    BiomePickerScreen(Screen parent, Picker picker, Map<String, BiomeInfo> biomes,
            Function<BiomeInfo, Situation> situation, Component now, String current, Consumer<String> onPick) {
        super(parent, picker, onPick);
        this.biomes = biomes;
        this.situation = situation;
        this.now = now;
        this.current = current;
        this.idle = biomes.containsKey(current) ? current : entries().get(0).id();
    }

    /** The starred and the recent biomes, each kind of biome that has any, and every biome. */
    @Override
    protected List<List<Shelf>> shelves() {
        return List.of(List.of(starredShelf(), recentShelf()), kinds(), List.of(every()));
    }

    /** A shelf for each kind of biome that has any, its picture the biome the kind names, else its first. */
    private List<Shelf> kinds() {
        List<Shelf> made = new ArrayList<>();
        for (BiomeKinds.Kind kind : BiomeKinds.ALL) {
            Function<List<Search.Entry>, List<Search.Entry>> of = shown -> ofKind(shown, kind);
            List<Search.Entry> any = of.apply(entries());
            if (!any.isEmpty()) {
                String pictured = kind.picture().filter(biomes::containsKey).orElse(any.get(0).id());
                made.add(new Shelf(Component.translatable(kind.name()), new BiomeIcon(biomes.get(pictured)), of));
            }
        }
        return made;
    }

    /** Every biome, its picture four of the game's biomes, each a little landscape, in a square. */
    private Shelf every() {
        Icon four = Icon.quartered(EVERY.stream().filter(biomes::containsKey).map(biomes::get)
                .<BiConsumer<Canvas, Rect>>map(biome -> (canvas, area) -> BiomeLandscape.draw(canvas, biome, area))
                .toList());
        return everyShelf(Component.translatable("flatgen.biomes.every"),
                (canvas, x, y) -> drawFramed(canvas, four, x, y));
    }

    /** A picture, framed as a biome's picture is. */
    private static void drawFramed(Canvas canvas, Icon picture, int x, int y) {
        picture.draw(canvas, x, y);
        canvas.outline(new Rect(x, y, Icon.SIZE, Icon.SIZE), BiomeIcon.RIM);
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

    /** The grid, and the panel of the biome looked at on the right. */
    @Override
    protected List<Widget> beside(Rect area) {
        Rect panel = new Rect(area.right() - PANEL_WIDTH, area.y(), PANEL_WIDTH, area.height());
        List<Widget> widgets = new ArrayList<>();
        widgets.add(grid(new Rect(area.x(), area.y(), panel.x() - GAP - area.x(), area.height()), CARD_WIDTH,
                CARD_HEIGHT, new Cards(), this::pick));
        widgets.addAll(panel(panel));
        return widgets;
    }

    /** The biome looked at: painted, its name and its mod, and its facts in words. */
    private List<Widget> panel(Rect panel) {
        Rect inside = new Rect(panel.x() + PANEL_PADDING, panel.y() + PANEL_PADDING,
                panel.width() - 2 * PANEL_PADDING, panel.height() - 2 * PANEL_PADDING);
        Rect picture = new Rect(inside.x(), inside.y(), inside.width(), PANEL_PICTURE);
        Rect name = new Rect(inside.x(), picture.bottom() + PANEL_PADDING, inside.width(),
                TextStyle.TITLE.lineHeight());
        Rect from = new Rect(inside.x(), name.bottom(), inside.width(), TextStyle.BODY.lineHeight());
        int factsTop = from.bottom() + PANEL_PADDING;
        return List.of(new Surface(Look.PANEL, panel), new Painted(() -> biomes.get(shown()), picture),
                new Caption(() -> Names.biome(shown()), name, TextStyle.TITLE, Look.TEXT),
                new Caption(() -> Component.literal(modName(biomes.get(shown()).namespace())), from, TextStyle.BODY,
                        Look.MUTED),
                new FactList(new Rect(inside.x(), factsTop, inside.width(), inside.bottom() - factsTop)));
    }

    /** The biome the panel shows: the one the mouse or the keyboard is on, else the biome now. */
    private String shown() {
        return grid().lookedAt().filter(biomes::containsKey).orElse(idle);
    }

    /** A biome's facts in the world as the editor has it, worked out once per biome while the picker is open. */
    private List<Fact.Shown> facts(String id) {
        return facts.computeIfAbsent(id, biome -> show(situation.apply(biomes.get(biome))));
    }

    private static List<Fact.Shown> show(Situation world) {
        return Facts.ABOUT_THE_BIOME.stream().map(fact -> fact.show(world)).toList();
    }

    /** The biome now, which the grid rings. */
    @Override
    protected boolean inUse(String id) {
        return id.equals(current);
    }

    /** A biome's card: its landscape over its name; its facts in its tooltip. */
    private final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            Rect picture = new Rect(place.x() + INSET, place.y() + INSET, place.width() - 2 * INSET, CARD_PICTURE);
            BiomeLandscape.draw(canvas, biomes.get(entry.id()), picture);
            Component name = canvas.fitted(Component.literal(entry.name()), picture.width(), TextStyle.BODY);
            canvas.text(name, picture.x(), TextStyle.BODY.top(picture.bottom(), place.bottom() - picture.bottom()),
                    Look.MUTED);
        }

        /** Its facts in words. */
        @Override
        public List<Component> describe(Search.Entry entry) {
            return facts(entry.id()).stream()
                    .flatMap(shown -> FactPictures.words(shown).stream()).toList();
        }
    }

    /** A biome painted over an area, as the supplier has it now, framed; a barrier for one the game lacks. */
    private static final class Painted implements Widget {
        private final Supplier<BiomeInfo> biome;
        private final Rect area;

        Painted(Supplier<BiomeInfo> biome, Rect area) {
            this.biome = biome;
            this.area = area;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            BiomeInfo shown = biome.get();
            if (shown == null) {
                Icon.MISSING.draw(canvas, area.x() + (area.width() - Icon.SIZE) / 2,
                        area.y() + (area.height() - Icon.SIZE) / 2);
            } else {
                BiomeLandscape.draw(canvas, shown, area);
            }
            canvas.outline(area, BiomeIcon.RIM);
        }
    }

    /** The facts of the biome looked at, each its picture beside its words, wrapped to the panel. */
    private final class FactList implements Widget {
        private final Rect area;

        FactList(Rect area) {
            this.area = area;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            int y = area.y();
            int wordsX = area.x() + Icon.SIZE + 2 * INSET;
            for (Fact.Shown shown : facts(shown())) {
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
