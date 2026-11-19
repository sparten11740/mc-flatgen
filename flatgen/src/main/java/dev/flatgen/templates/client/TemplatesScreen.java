package dev.flatgen.templates.client;

import dev.flatgen.blocks.client.BlockPicker;
import dev.flatgen.client.gui.Badge;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Slider;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.WidgetGroup;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.PageFrame;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.client.FactPictures;
import dev.flatgen.facts.client.FactsNow;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.layers.client.ColumnPicture;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Names;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.templates.rules.Parameter;
import dev.flatgen.templates.rules.Template;
import dev.flatgen.templates.rules.Templates;
import dev.flatgen.templates.rules.Values;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Templates (F14), opened from the editor: a picture for each template; the one chosen shows its settings as sliders,
 * block buttons and switches, with its column and the world's facts drawn as they change; Use puts the result in the
 * editor as one step to undo.
 */
public final class TemplatesScreen extends FlatgenScreen {
    /** A row of the settings, and the space between controls, in GUI pixels. */
    private static final int ROW_HEIGHT = 20;
    private static final int GAP = 4;
    /** The width of the column's preview and of the settings' controls, in GUI pixels. */
    private static final int PREVIEW_WIDTH = 32;
    private static final int CONTROL_WIDTH = 200;
    /** The settings' box: its widest, and the space inside it, in GUI pixels. */
    private static final int PADDING = 8;
    private static final int CONTROLS_WIDTH = CONTROL_WIDTH + 2 * PADDING;
    /** How wide Back and Use are, in GUI pixels. */
    private static final int BUTTON_WIDTH = 70;

    private final EditorContext editor;
    /** The world the template makes now, as the facts need it. */
    private final FactsNow facts;
    private Template template = Templates.ALL.get(0);
    private Values values = Values.defaults(template.parameters());
    /** What the template made for the values it was last asked with, kept until the template or a value changes. */
    private List<Object> madeFor = List.of();
    private Preset made;

    /** The templates, for the editor that opened them. */
    public TemplatesScreen(EditorContext editor) {
        super(Component.translatable("flatgen.templates.title"));
        this.editor = editor;
        this.facts = new FactsNow(editor, this::made);
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        PageFrame frame = new PageFrame(width, height);
        Rect body = frame.body();
        FlowLayout row = new FlowLayout(new Rect(body.x(), body.y(), body.width(), 0), PictureButton.SIZE, GAP);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new WidgetGroup(Templates.ALL.stream().<Widget>map(choice -> new PictureButton(
                Icon.item(choice.icon()), row.next(PictureButton.SIZE), () -> choose(choice),
                () -> List.of(Component.translatable(choice.name())))).toList()));
        int top = row.bottom() + GAP;
        widgets.add(new Label(Icon.item(template.icon()), Component.translatable(template.name()),
                new Rect(body.x(), top, body.width(), Label.height())));
        top += Label.height() + GAP;
        widgets.add(new Preview(new Rect(body.x(), top, PREVIEW_WIDTH, body.bottom() - top)));
        int controlsX = body.x() + PREVIEW_WIDTH + PageFrame.GAP;
        Rect controlsArea = new Rect(controlsX, top, Math.min(CONTROLS_WIDTH, body.right() - controlsX),
                body.bottom() - top);
        widgets.add(new Surface(Look.PANEL, controlsArea));
        FlowLayout controls = new FlowLayout(new Rect(controlsArea.x() + PADDING, top + PADDING,
                controlsArea.width() - 2 * PADDING, 0), ROW_HEIGHT, GAP);
        List<Widget> settings = new ArrayList<>();
        Facts.ALL.forEach(fact -> settings.add(new Badge(controls.next(Badge.SIZE),
                () -> facts.get().map(world -> new Badge.Look(FactPictures.icon(fact.show(world)),
                        FactPictures.words(fact.show(world)))))));
        template.parameters().forEach(parameter -> settings.add(parameter.accept(new Control(controls))));
        widgets.add(new WidgetGroup(settings));
        List<Rect> actions = frame.actions(BUTTON_WIDTH, BUTTON_WIDTH);
        widgets.add(new WidgetGroup(List.of(
                new TextButton(Icon.BACK, CommonComponents.GUI_BACK, actions.get(0), this::onClose),
                new TextButton(Icon.line("check"), () -> Component.translatable("flatgen.templates.use"),
                        actions.get(1), this::use, () -> true,
                        () -> List.of(Component.translatable("flatgen.templates.use_tip")))
                        .kind(Look.ButtonKind.PRIMARY))));
        return widgets;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(editor.screen());
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

    /** The column the template makes now, drawn small. */
    private final class Preview implements Widget {
        private final Rect bounds;

        Preview(Rect bounds) {
            this.bounds = bounds;
        }

        @Override
        public void draw(Canvas canvas, int mouseX, int mouseY) {
            Preset preset = made();
            ColumnPicture.draw(canvas, preset.layers(), Biomes.waterColor(preset.biome(), editor.registries()),
                    bounds);
        }
    }

    /** Each kind of setting as its control: a slider, a block button or a switch. */
    private final class Control implements Parameter.Visitor<Widget> {
        private final FlowLayout flow;

        Control(FlowLayout flow) {
            this.flow = flow;
        }

        @Override
        public Widget number(Parameter.Number number) {
            flow.newRow();
            Label picture = new Label(Icon.item(number.icon()), Component.empty(), flow.next(Icon.SIZE));
            Slider slider = new Slider(number.id(), flow.next(CONTROL_WIDTH - Icon.SIZE - GAP), number.min(),
                    number.max(), () -> values.number(number), value -> values = values.with(number, value),
                    () -> Component.translatable(number.name(), values.number(number)));
            return new WidgetGroup(List.of(picture, slider));
        }

        @Override
        public Widget block(Parameter.Block block) {
            flow.newRow();
            return new TextButton((canvas, x, y) -> canvas.blockIcon(values.block(block), x, y),
                    () -> Component.translatable(block.name(), Names.block(values.block(block))),
                    flow.next(CONTROL_WIDTH), () -> pick(block), () -> true,
                    () -> List.of(Component.translatable("flatgen.templates.pick_block")));
        }

        @Override
        public Widget toggle(Parameter.Toggle toggle) {
            flow.newRow();
            return new Toggle(Icon.item(toggle.icon()), Component.translatable(toggle.name()),
                    flow.next(CONTROL_WIDTH), () -> values.toggle(toggle),
                    () -> values = values.with(toggle, !values.toggle(toggle)), () -> true, List::of);
        }

        private void pick(Parameter.Block block) {
            minecraft.setScreen(BlockPicker.screen(TemplatesScreen.this, editor.enabledFeatures(),
                    picked -> values = values.with(block, picked)));
        }
    }
}
