package dev.flatgen.templates.client;

import dev.flatgen.blocks.client.BlockPicker;
import dev.flatgen.client.gui.Badge;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Slider;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.client.FactPictures;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.facts.rules.Situation;
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
import java.util.Optional;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Templates (F14), opened from the editor: a picture for each template; the one chosen shows its settings as sliders,
 * block buttons and switches, with its column and the world's facts drawn as they change; Use puts the result in the
 * editor as one step to undo.
 */
public final class TemplatesScreen extends FlatgenScreen {
    private static final int MARGIN = 12;
    /** Where the templates' pictures start, below the title, in GUI pixels. */
    private static final int ROWS_TOP = 22;
    private static final int ROW_HEIGHT = 20;
    private static final int GAP = 4;
    /** The width of the column's preview and of the settings' controls, in GUI pixels. */
    private static final int PREVIEW_WIDTH = 32;
    private static final int CONTROL_WIDTH = 200;
    private static final int BUTTON_WIDTH = 70;
    /** Space below the settings for Use and Back, in GUI pixels. */
    private static final int FOOTER = 32;

    private final EditorContext editor;
    private Template template = Templates.ALL.get(0);
    private Values values = Values.defaults(template.parameters());
    /** What the template made for the values it was last asked with, kept until the template or a value changes. */
    private List<Object> madeFor = List.of();
    private Preset made;
    private Optional<Situation> situation = Optional.empty();

    /** The templates, for the editor that opened them. */
    public TemplatesScreen(EditorContext editor) {
        super(Component.translatable("flatgen.templates.title"));
        this.editor = editor;
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        FlowLayout row = new FlowLayout(new Rect(MARGIN, ROWS_TOP, width - 2 * MARGIN, 0), PictureButton.SIZE, GAP);
        List<Widget> widgets = new ArrayList<>();
        Templates.ALL.forEach(choice -> widgets.add(new PictureButton(Icon.item(choice.icon()),
                row.next(PictureButton.SIZE), () -> choose(choice),
                () -> List.of(Component.translatable(choice.name())))));
        int top = row.bottom() + GAP;
        widgets.add(new Label(Icon.item(template.icon()), Component.translatable(template.name()),
                new Rect(MARGIN, top, width - 2 * MARGIN, Label.height())));
        top += Label.height() + GAP;
        int previewHeight = height - top - FOOTER;
        widgets.add(new Preview(new Rect(MARGIN, top, PREVIEW_WIDTH, previewHeight)));
        int controlsX = MARGIN + PREVIEW_WIDTH + 2 * MARGIN;
        FlowLayout controls = new FlowLayout(new Rect(controlsX, top, width - controlsX - MARGIN, 0), ROW_HEIGHT, GAP);
        Facts.ALL.forEach(fact -> widgets.add(new Badge(controls.next(Badge.SIZE),
                () -> situation().map(world -> new Badge.Look(FactPictures.icon(fact.show(world)),
                        FactPictures.words(fact.show(world)))))));
        template.parameters().forEach(parameter -> widgets.add(parameter.accept(new Control(controls))));
        FlowLayout footer = new FlowLayout(new Rect((width - 2 * BUTTON_WIDTH - GAP) / 2,
                height - (FOOTER + ROW_HEIGHT) / 2, 2 * BUTTON_WIDTH + GAP, 0), ROW_HEIGHT, GAP);
        widgets.add(new TextButton(Component.translatable("flatgen.templates.use"), footer.next(BUTTON_WIDTH),
                this::use, () -> true, () -> List.of(Component.translatable("flatgen.templates.use_tip"))));
        widgets.add(new TextButton(CommonComponents.GUI_BACK, footer.next(BUTTON_WIDTH), this::onClose));
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

    private void use() {
        editor.use(made());
        onClose();
    }

    /** The preset the template makes now, made again only when the template or a value changed. */
    private Preset made() {
        List<Object> key = List.of(template, values);
        if (!key.equals(madeFor)) {
            madeFor = key;
            made = template.make(values, editor.world());
            situation = Biomes.info(made.biome(), editor.registries())
                    .map(biome -> Situation.of(biome, editor.world().floor() + made.height(), true));
        }
        return made;
    }

    /** The world the template makes now, as the facts need it; none for a biome the game lacks. */
    private Optional<Situation> situation() {
        made();
        return situation;
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
            return new Slider(number.id(), flow.next(CONTROL_WIDTH), number.min(), number.max(),
                    () -> values.number(number), value -> values = values.with(number, value),
                    () -> Component.translatable(number.name(), values.number(number)));
        }

        @Override
        public Widget block(Parameter.Block block) {
            flow.newRow();
            return new TextButton(() -> Component.translatable(block.name(), Names.block(values.block(block))),
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
