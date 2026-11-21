package dev.flatgen.templates.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.blocks.client.BlockPicker;
import dev.flatgen.client.gui.Badge;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Chip;
import dev.flatgen.client.gui.ChoiceCard;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Slider;
import dev.flatgen.client.gui.Stepper;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextLines;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.WidgetGroup;
import dev.flatgen.client.gui.rules.PageFrame;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.dimensions.rules.DimensionColours;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.client.FactPictures;
import dev.flatgen.facts.client.FactsNow;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.layers.client.ColumnPicture;
import dev.flatgen.layers.client.LayerText;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Names;
import dev.flatgen.mc.Screens;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.templates.rules.Parameter;
import dev.flatgen.templates.rules.Template;
import dev.flatgen.templates.rules.Templates;
import dev.flatgen.templates.rules.Values;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Templates (F14), opened from the editor, as round 3 of docs/design/simple-editor.md drew them: the templates down the
 * left, each a little picture of what it makes, its name and what it is; the one chosen in the middle with its settings
 * as sliders with steps, block buttons and switches; and at the right what it makes, drawn as it changes, with the
 * world's facts and its layers top first. Use puts it in the editor as one step to undo.
 */
public final class TemplatesScreen extends FlatgenScreen {
    /** Space between the screen's parts, and between rows, in GUI pixels. */
    private static final int GAP = 6;
    /** Widths in GUI pixels: the templates down the left and what the chosen one makes at the right. */
    private static final int LIST_WIDTH = 160;
    private static final int PREVIEW_WIDTH = 150;
    /** How far below the top of a card's picture the template's item sits, in GUI pixels. */
    private static final int CARD_ICON_TOP = 1;
    /** The most lines a template's card shows of what it is. */
    private static final int ABOUT_LINES = 2;
    /** A row of the settings, in GUI pixels. */
    private static final int ROW = 20;
    /** The sky's share of the preview, in hundredths of its height, and the thinnest the ground is drawn, in pixels. */
    private static final int SKY_SHARE = 55;
    private static final int THINNEST_GROUND = 4;
    /** Space inside the preview, and a layer's row in its list, in GUI pixels. */
    private static final int PREVIEW_PADDING = 6;
    private static final int LAYER_ROW = 18;
    private static final int WHOLE = 100;

    private final EditorContext editor;
    /** The world the template makes now, as the facts need it. */
    private final FactsNow facts;
    private Template template = Templates.ALL.get(0);
    private Values values = Values.defaults(template.parameters());
    /** What the template made for the values it was last asked with, kept until the template or a value changes. */
    private List<Object> madeFor = List.of();
    private Preset made;
    /** What each template makes with its settings as they start, for its card, worked out once. */
    private final Map<Template, Preset> madeAtFirst = new HashMap<>();

    /** The templates, for the editor that opened them. */
    public TemplatesScreen(EditorContext editor) {
        super(Component.translatable("flatgen.templates.title"));
        this.editor = editor;
        this.facts = new FactsNow(editor, this::made);
    }

    /** The header, the templates, the chosen one's settings, what it makes, and the footer. */
    @Override
    protected List<Widget> layOut(int width, int height) {
        PageFrame frame = new PageFrame(width, height);
        Rect body = frame.body();
        Rect preview = new Rect(body.right() - PREVIEW_WIDTH, body.y(), PREVIEW_WIDTH, body.height());
        int settingsX = body.x() + LIST_WIDTH + PageFrame.GAP;
        List<Widget> widgets = new ArrayList<>(header());
        widgets.add(new WidgetGroup(cards(new Rect(body.x(), body.y(), LIST_WIDTH, body.height()))));
        widgets.add(new WidgetGroup(settings(new Rect(settingsX, body.y(), preview.x() - PageFrame.GAP - settingsX,
                body.height()))));
        widgets.addAll(preview(preview));
        widgets.addAll(footer(frame));
        return widgets;
    }

    /** Nothing but the frame's lines: the title is a widget after Back. */
    @Override
    protected void drawBehind(Canvas canvas) {
        drawFrameLines(canvas);
    }

    @Override
    public void onClose() {
        Screens.show(editor.screen());
    }

    /** Back, then the title over what templates are for. */
    private List<Widget> header() {
        PictureButton back = backButton();
        int textX = back.bounds().right() + GAP;
        int title = TextStyle.TITLE.lineHeight();
        int line = TextStyle.BODY.lineHeight();
        int top = (PageFrame.HEADER - title - line) / 2;
        int textWidth = width - PageFrame.MARGIN - textX;
        return List.of(back,
                new Caption(this::getTitle, new Rect(textX, top, textWidth, title), TextStyle.TITLE, Look.TEXT),
                new Caption(() -> Component.translatable("flatgen.templates.open_tip"), new Rect(textX, top + title,
                        textWidth, line), TextStyle.BODY, Look.MUTED));
    }

    /** A card for each template, one under another: what it makes, its name and what it is. */
    private List<Widget> cards(Rect area) {
        List<Widget> cards = new ArrayList<>();
        int cardHeight = ChoiceCard.height(ABOUT_LINES);
        int y = area.y();
        for (Template each : Templates.ALL) {
            List<Component> about = wrapped(Component.translatable(each.about()), ChoiceCard.wordsWidth(area.width()));
            cards.add(new ChoiceCard((canvas, place) -> drawCardPicture(canvas, each, place),
                    Component.translatable(each.name()), about,
                    new Rect(area.x(), y, area.width(), cardHeight), () -> each == template, () -> choose(each)));
            y += cardHeight + GAP;
        }
        return cards;
    }

    /** The chosen template's name over what it is, then a row for each of its settings. */
    private List<Widget> settings(Rect area) {
        int title = TextStyle.TITLE.lineHeight();
        List<Component> about = wrapped(Component.translatable(template.about()), area.width());
        int aboutHeight = TextLines.height(about.size(), TextStyle.BODY.lineHeight());
        List<Widget> widgets = new ArrayList<>(List.of(
                new Caption(() -> Component.translatable(template.name()), new Rect(area.x(), area.y(), area.width(),
                        title), TextStyle.TITLE, Look.TEXT),
                new TextLines(new Rect(area.x(), area.y() + title, area.width(), aboutHeight), () -> about)));
        int y = area.y() + title + aboutHeight + 2 * GAP;
        if (template.parameters().isEmpty()) {
            widgets.add(new Caption(() -> Component.translatable("flatgen.templates.no_settings"),
                    new Rect(area.x(), y, area.width(), ROW), TextStyle.BODY, Look.MUTED));
        }
        for (Parameter parameter : template.parameters()) {
            widgets.add(parameter.accept(new SettingRow(new Rect(area.x(), y, area.width(), ROW))));
            y += ROW + GAP;
        }
        return widgets;
    }

    /**
     * What the template makes: its ground in the sky, with the air above it on a chip; the world's facts; and its
     * layers, top first.
     */
    private List<Widget> preview(Rect panel) {
        Rect inside = new Rect(panel.x() + PREVIEW_PADDING, panel.y() + PREVIEW_PADDING,
                panel.width() - 2 * PREVIEW_PADDING, panel.height() - 2 * PREVIEW_PADDING);
        Rect sky = new Rect(inside.x(), inside.y(), inside.width(), inside.height() * SKY_SHARE / WHOLE);
        int badgesTop = sky.bottom() + GAP;
        List<Widget> badges = new ArrayList<>();
        for (int index = 0; index < Facts.ALL.size(); index++) {
            Rect place = new Rect(inside.x() + index * (Badge.SIZE + GAP / 2), badgesTop, Badge.SIZE, Badge.SIZE);
            badges.add(new Badge(place, factLook(index)));
        }
        int listTop = badgesTop + Badge.SIZE + GAP;
        int line = TextStyle.BODY.lineHeight();
        return List.of(new Surface(Look.PANEL, panel),
                (canvas, mouseX, mouseY) -> drawGround(canvas, made(), sky),
                Chip.inCorner(Icon.line("arrow_up"), this::air, sky),
                new WidgetGroup(badges),
                new Caption(() -> Component.translatable("flatgen.templates.layers"), new Rect(inside.x(), listTop,
                        inside.width(), line), TextStyle.BODY, Look.MUTED),
                new LayerList(new Rect(inside.x(), listTop + line + GAP / 2, inside.width(),
                        inside.bottom() - listTop - line - GAP / 2)));
    }

    /** How a fact of the world the template makes looks on its badge, by its place among the facts. */
    private Supplier<Optional<Badge.Look>> factLook(int index) {
        return () -> facts.get().map(world -> Facts.ALL.get(index).show(world))
                .map(shown -> new Badge.Look(FactPictures.icon(shown), FactPictures.words(shown)));
    }

    /** The note on what Use does at the left; Cancel and Use at the right. */
    private List<Widget> footer(PageFrame frame) {
        Component use = Component.translatable("flatgen.templates.use");
        List<Rect> actions = frame.actions(actionWidth(CommonComponents.GUI_CANCEL), actionWidth(use));
        Rect row = frame.footerRow();
        return List.of(new Caption(() -> Component.translatable("flatgen.templates.use_note"),
                        new Rect(row.x(), row.y(), actions.get(0).x() - GAP - row.x(), row.height()), TextStyle.BODY,
                        Look.MUTED),
                new TextButton(Icon.line("cross"), CommonComponents.GUI_CANCEL, actions.get(0), this::onClose),
                new TextButton(Icon.line("check"), () -> use, actions.get(1), this::use, () -> true,
                        () -> List.of(Component.translatable("flatgen.templates.use_tip")))
                        .kind(Look.ButtonKind.PRIMARY));
    }

    private void choose(Template chosen) {
        template = chosen;
        values = Values.defaults(chosen.parameters());
        rebuildWidgets();
    }

    /** Puts the template's world in the editor, keeping its Nether and End, of which a template says nothing (F22). */
    private void use() {
        editor.use(made().withOtherStacks(editor.shown().preset()::stack));
        onClose();
    }

    /** The preset the template makes now, made again only when the template or a value changed. */
    private Preset made() {
        List<Object> key = List.of(template, values);
        if (!key.equals(madeFor)) {
            madeFor = key;
            made = template.make(values, editor.world());
        }
        return made;
    }

    /** What a template makes as its card shows it: the chosen one as set now, the others as they start. */
    private Preset madeBy(Template shown) {
        return shown == template ? made()
                : madeAtFirst.computeIfAbsent(shown, each -> each.make(Values.defaults(each.parameters()),
                        editor.world()));
    }

    /** The blocks of air above the ground the template makes. */
    private Component air() {
        return LayerText.air(editor.world(), made().height());
    }

    /** A template card's picture: what it makes, and the template's own item over its sky. */
    private void drawCardPicture(Canvas canvas, Template shown, Rect place) {
        drawGround(canvas, madeBy(shown), place);
        canvas.itemIcon(shown.icon(), place.x() + (place.width() - Icon.SIZE) / 2, place.y() + CARD_ICON_TOP);
    }

    /** A preset's ground standing in its biome's sky, as tall against the sky as against the world. */
    private void drawGround(Canvas canvas, Preset preset, Rect sky) {
        canvas.fill(sky, Biomes.info(preset.biome(), editor.registries()).map(BiomeInfo::colours)
                .map(BiomeInfo.Colours::skyArgb).orElse(DimensionColours.OVERWORLD_SKY));
        int ground = Math.max(THINNEST_GROUND, sky.height() * preset.height() / Math.max(1, editor.world().height()));
        ColumnPicture.draw(canvas, preset.layers(), Biomes.waterColor(preset.biome(), editor.registries()),
                new Rect(sky.x(), sky.bottom() - ground, sky.width(), ground));
    }

    /** The layers the template makes, top first, each its block's picture, its name and how thick it is. */
    private final class LayerList implements Widget {
        private final Rect area;

        LayerList(Rect area) {
            this.area = area;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            List<Layer> layers = made().layers();
            int fit = Math.max(1, area.height() / LAYER_ROW);
            int shown = layers.size() > fit ? fit - 1 : layers.size();
            for (int index = 0; index < shown; index++) {
                drawLayer(canvas, layers.get(layers.size() - 1 - index), area.y() + index * LAYER_ROW);
            }
            if (shown < layers.size()) {
                canvas.text(Component.translatable("flatgen.templates.more_layers", layers.size() - shown), area.x(),
                        TextStyle.BODY.top(area.y() + shown * LAYER_ROW, LAYER_ROW), Look.MUTED);
            }
        }

        private void drawLayer(Canvas canvas, Layer layer, int y) {
            canvas.blockIcon(layer.block(), area.x(), y + (LAYER_ROW - Icon.SIZE) / 2);
            Component count = Component.literal(Integer.toString(layer.height()));
            int countWidth = canvas.textWidth(count, TextStyle.STRONG);
            int nameX = area.x() + Icon.SIZE + GAP;
            canvas.text(canvas.fitted(Names.block(layer.block()), area.right() - countWidth - GAP - nameX,
                    TextStyle.BODY), nameX, TextStyle.BODY.top(y, LAYER_ROW), Look.TEXT);
            canvas.text(count, area.right() - countWidth, TextStyle.STRONG.top(y, LAYER_ROW), Look.TEXT,
                    TextStyle.STRONG);
        }
    }

    /** Each kind of setting as its row: a slider with steps, a block button or a switch. */
    private final class SettingRow implements Parameter.Visitor<Widget> {
        private final Rect place;

        SettingRow(Rect place) {
            this.place = place;
        }

        /** Its picture, a slider showing its name and value, and a step down and up in one box. */
        @Override
        public Widget number(Parameter.Number number) {
            int buttonTop = place.y() + (place.height() - PictureButton.SIZE) / 2;
            Rect raise = new Rect(place.right() - PictureButton.SIZE, buttonTop, PictureButton.SIZE,
                    PictureButton.SIZE);
            Rect lower = new Rect(raise.x() - PictureButton.SIZE, buttonTop, PictureButton.SIZE, PictureButton.SIZE);
            int sliderX = place.x() + Icon.SIZE + GAP;
            List<Widget> row = new ArrayList<>(List.of(
                    new Label(Icon.itemOrTexture(number.icon()), Component.empty(), new Rect(place.x(), place.y(),
                            Icon.SIZE, place.height())),
                    new Slider(number.id(), new Rect(sliderX, place.y(), lower.x() - GAP - sliderX, place.height()),
                            number.min(), number.max(), () -> values.number(number),
                            value -> values = values.with(number, value),
                            () -> Component.translatable(number.name(), values.number(number)))));
            row.addAll(Stepper.of(step(number, Icon.line("minus"), "flatgen.templates.lower", -1, lower),
                    Optional.empty(), step(number, Icon.line("plus"), "flatgen.templates.raise", 1, raise)));
            return new WidgetGroup(row);
        }

        /** A button stepping a number by this much, greyed where its range ends. */
        private PictureButton step(Parameter.Number number, Icon icon, String key, int by, Rect where) {
            return new PictureButton(icon, where, () -> values = values.with(number, values.number(number) + by),
                    () -> number.within(values.number(number) + by) != values.number(number),
                    () -> List.of(Component.translatable(key), Component.translatable(number.name(),
                            values.number(number))));
        }

        @Override
        public Widget block(Parameter.Block block) {
            return new TextButton((canvas, x, y) -> canvas.blockIcon(values.block(block), x, y),
                    () -> Component.translatable(block.name(), Names.block(values.block(block))), place,
                    () -> pick(block), () -> true,
                    () -> List.of(Component.translatable("flatgen.templates.pick_block")));
        }

        @Override
        public Widget toggle(Parameter.Toggle toggle) {
            return new Toggle(Icon.itemOrTexture(toggle.icon()), Component.translatable(toggle.name()), place,
                    () -> values.toggle(toggle), () -> values = values.with(toggle, !values.toggle(toggle)),
                    () -> true, List::of);
        }

        private void pick(Parameter.Block block) {
            Screens.show(BlockPicker.screen(TemplatesScreen.this, editor.enabledFeatures(), editor.registries(),
                    picked -> values = values.with(block, picked)));
        }
    }
}
