package dev.flatgen.library.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.QuestionScreen;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextField;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.layers.client.ColumnPicture;
import dev.flatgen.library.PresetStore;
import dev.flatgen.library.rules.ApplyPreset;
import dev.flatgen.library.rules.MissingParts;
import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

/**
 * The preset library (F11), opened from the editor: the player's saved presets as cards (a small picture of the column,
 * the top block's icon and the name; a barrier on one naming parts the game lacks or that cannot be read). The editor's
 * preset can be saved as a new preset or over one; a card picked can be applied (a step to undo), renamed, duplicated
 * or deleted. Overwriting, deleting and applying one with missing parts ask first.
 */
public final class LibraryScreen extends FlatgenScreen {
    private static final int MARGIN = 12;
    /** Where the controls start, below the title, in GUI pixels. */
    private static final int ROWS_TOP = 22;
    private static final int ROW_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int BUTTON_WIDTH = 70;
    private static final int NAME_WIDTH = 120;
    /** A card's size, and the size of its column picture, in GUI pixels. */
    private static final int CARD_WIDTH = 64;
    private static final int CARD_HEIGHT = 64;
    private static final int PICTURE_WIDTH = 16;
    private static final int PICTURE_HEIGHT = 40;
    private static final int PADDING = 4;
    private static final int SELECTED = 0xFFFFFFFF;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final Icon WARNING = Icon.item("minecraft:barrier");
    /** The name a new preset starts with, numbered when taken. */
    private static final String NEW_NAME = "Preset";

    private final EditorContext editor;
    private final PresetStore store;
    private List<PresetLibrary.Entry> entries = List.of();
    /** The parts each preset names that the game lacks, by file, worked out as the presets are read. */
    private final Map<String, MissingIds> missing = new HashMap<>();
    /** The file of the card picked; none before one is. */
    private Optional<String> picked = Optional.empty();
    private String name = "";
    private TextField nameField;

    /** The library of the player's presets, for the editor that opened it. */
    public LibraryScreen(EditorContext editor) {
        super(Component.translatable("flatgen.library.title"));
        this.editor = editor;
        this.store = PresetStore.player();
        reload();
        this.name = PresetLibrary.newName(NEW_NAME, names());
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        FlowLayout rows = new FlowLayout(new Rect(MARGIN, ROWS_TOP, width - 2 * MARGIN, 0), ROW_HEIGHT, GAP);
        nameField = new TextField("name", rows.next(NAME_WIDTH), Component.translatable("flatgen.library.name"), name,
                text -> name = text);
        List<Widget> widgets = new ArrayList<>(List.of(nameField,
                button("flatgen.library.save_new", rows, this::saveNew, () -> !name.isBlank()),
                button("flatgen.library.rename", rows, this::rename, () -> pickedPreset().isPresent()
                        && !name.isBlank())));
        rows.newRow();
        widgets.add(button("flatgen.library.apply", rows, this::apply, () -> pickedPreset().isPresent()));
        widgets.add(button("flatgen.library.overwrite", rows, this::overwrite, () -> picked.isPresent()));
        widgets.add(button("flatgen.library.duplicate", rows, this::duplicate, () -> pickedPreset().isPresent()));
        widgets.add(button("flatgen.library.delete", rows, this::delete, () -> picked.isPresent()));
        widgets.add(new TextButton(Component.translatable("flatgen.library.back"), rows.next(BUTTON_WIDTH),
                this::onClose));
        int gridTop = rows.bottom() + GAP;
        widgets.add(new CardGrid(new Rect(MARGIN, gridTop, width - 2 * MARGIN, height - gridTop - MARGIN),
                CARD_WIDTH, CARD_HEIGHT, this::sections, new Cards(), this::pick));
        return widgets;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(editor.screen());
    }

    private TextButton button(String key, FlowLayout rows, Runnable action, BooleanSupplier usable) {
        return new TextButton(Component.translatable(key), rows.next(BUTTON_WIDTH), action, usable,
                () -> List.of(Component.translatable(key + "_tip")));
    }

    private List<CardGrid.Section> sections() {
        return List.of(new CardGrid.Section(Component.translatable("flatgen.library.presets"), entries.stream()
                .map(entry -> new Search.Entry(entry.file(), entry.name(), "")).toList()));
    }

    private void pick(String file) {
        picked = Optional.of(file);
        entry(file).ifPresent(entry -> nameField.show(entry.name()));
    }

    private void saveNew() {
        store.saveNew(withTopIcon(editor.shown().preset()).withName(name.strip())).ifPresent(this::reloadPicking);
    }

    private void rename() {
        pickedPreset().ifPresent(preset -> store.rename(picked.get(), preset, name.strip())
                .ifPresent(this::reloadPicking));
    }

    private void duplicate() {
        pickedPreset().ifPresent(preset -> store.duplicate(preset).ifPresent(this::reloadPicking));
    }

    private void overwrite() {
        String file = picked.get();
        String shownName = entry(file).map(PresetLibrary.Entry::name).orElse(file);
        ask("flatgen.library.overwrite_question", shownName, () -> store.overwrite(file,
                withTopIcon(editor.shown().preset()).withName(shownName)));
    }

    private void delete() {
        String file = picked.get();
        ask("flatgen.library.delete_question", entry(file).map(PresetLibrary.Entry::name).orElse(file),
                () -> store.delete(file));
    }

    /** Applies the preset picked, asking first to replace the parts the game lacks; then back to the editor. */
    private void apply() {
        Preset preset = pickedPreset().orElseThrow();
        MissingIds lacking = missing.getOrDefault(picked.get(), MissingIds.NONE);
        if (lacking.isEmpty()) {
            applyAndReturn(preset);
            return;
        }
        minecraft.setScreen(new QuestionScreen(this, Component.translatable("flatgen.library.missing_title"),
                List.of(Component.translatable("flatgen.library.missing_question", listed(lacking))),
                () -> applyAndReturn(MissingParts.replaced(preset, lacking))));
    }

    private void applyAndReturn(Preset preset) {
        editor.apply(new ApplyPreset(preset));
        if (!preset.fitsWorld(editor.world().height())) {
            editor.say(Component.translatable("flatgen.library.too_tall", editor.world().height()));
        }
        minecraft.setScreen(editor.screen());
    }

    /** The ids a preset names that the game lacks, in a line. */
    private static String listed(MissingIds lacking) {
        return String.join(", ", Stream.of(lacking.blocks(), lacking.biomes(), lacking.structureSets())
                .flatMap(List::stream).toList());
    }

    /** Asks before something that cannot be undone, about a preset of this name, then reads the presets again. */
    private void ask(String key, String presetName, Runnable yes) {
        minecraft.setScreen(new QuestionScreen(this, Component.translatable("flatgen.library.title"),
                List.of(Component.translatable(key, presetName)), () -> afterwards(yes)));
    }

    private void afterwards(Runnable change) {
        change.run();
        picked = Optional.empty();
        reload();
    }

    private void reloadPicking(String file) {
        reload();
        pick(file);
    }

    private void reload() {
        entries = PresetLibrary.ordered(store.load(), Set.of(), List.of());
        missing.clear();
        entries.forEach(entry -> entry.preset().ifPresent(preset -> missing.put(entry.file(),
                FlatSettings.missingIds(preset, editor.registries()))));
    }

    private Optional<PresetLibrary.Entry> entry(String file) {
        return entries.stream().filter(entry -> entry.file().equals(file)).findFirst();
    }

    private Optional<Preset> pickedPreset() {
        return picked.flatMap(this::entry).flatMap(PresetLibrary.Entry::preset);
    }

    private List<String> names() {
        return entries.stream().map(PresetLibrary.Entry::name).toList();
    }

    /** A preset with its top layer's block as its icon, as the library shows it. */
    private static Preset withTopIcon(Preset preset) {
        List<Layer> layers = preset.layers();
        return layers.isEmpty() ? preset : preset.withIcon(layers.get(layers.size() - 1).block());
    }

    /** A preset's card: its column, its icon and its name, a barrier when it is broken or names missing parts. */
    private final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry card, Rect place) {
            Optional<PresetLibrary.Entry> entry = entry(card.id());
            Optional<Preset> preset = entry.flatMap(PresetLibrary.Entry::preset);
            int x = place.x() + PADDING;
            int y = place.y() + PADDING;
            preset.ifPresent(found -> ColumnPicture.draw(canvas, found.layers(),
                    Biomes.waterColor(found.biome(), editor.registries()), new Rect(x, y, PICTURE_WIDTH,
                            PICTURE_HEIGHT)));
            preset.ifPresent(found -> canvas.blockIcon(found.icon(), x + PICTURE_WIDTH + PADDING, y));
            boolean warned = preset.isEmpty() || !missing.getOrDefault(card.id(), MissingIds.NONE).isEmpty();
            if (warned) {
                WARNING.draw(canvas, x + PICTURE_WIDTH + PADDING, y + Icon.SIZE + PADDING);
            }
            canvas.clip(new Rect(place.x(), place.y(), place.width(), place.height()));
            canvas.text(Component.literal(card.name()), x, y + PICTURE_HEIGHT + PADDING, NAME_COLOR);
            canvas.unclip();
            if (picked.filter(card.id()::equals).isPresent()) {
                canvas.outline(place, SELECTED);
            }
        }

        @Override
        public List<Component> describe(Search.Entry card) {
            List<Component> lines = new ArrayList<>();
            entry(card.id()).ifPresent(entry -> entry.problems().forEach(problem -> lines.add(
                    Component.literal(problem))));
            MissingIds lacking = missing.getOrDefault(card.id(), MissingIds.NONE);
            if (!lacking.isEmpty()) {
                lines.add(Component.translatable("flatgen.library.missing", listed(lacking)));
            }
            return lines;
        }
    }
}
