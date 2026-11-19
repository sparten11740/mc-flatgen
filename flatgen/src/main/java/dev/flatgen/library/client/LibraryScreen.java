package dev.flatgen.library.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.CycleButton;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.QuestionScreen;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextField;
import dev.flatgen.client.gui.TextLines;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.layers.client.ColumnPicture;
import dev.flatgen.library.ChoicesFile;
import dev.flatgen.library.LibrarySources;
import dev.flatgen.library.PresetImports;
import dev.flatgen.library.PresetStore;
import dev.flatgen.library.rules.Choices;
import dev.flatgen.library.rules.PresetLibrary;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Clipboard;
import dev.flatgen.mc.FlatSettings;
import dev.flatgen.mc.SystemFiles;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.search.rules.Search;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;
import java.util.Set;
import java.util.function.BooleanSupplier;
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
    /** The width of a button with a word, in GUI pixels: the name row fits across the narrowest screen. */
    private static final int BUTTON_WIDTH = 56;
    private static final int NAME_WIDTH = 100;
    /** Space keeping Save over and Delete, which cannot be undone, apart from the other buttons, in GUI pixels. */
    private static final int DANGER_GAP = 8;
    /** A card's size, and the size of its column picture, in GUI pixels. */
    private static final int CARD_WIDTH = 64;
    private static final int CARD_HEIGHT = 64;
    private static final int PICTURE_WIDTH = 16;
    private static final int PICTURE_HEIGHT = 40;
    private static final int PADDING = 4;
    private static final int SELECTED = 0xFFFFFFFF;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final Icon WARNING = Icon.item("minecraft:barrier");
    /** The mark of the default preset, the one Superflat starts with. */
    private static final Icon DEFAULT = Icon.item("minecraft:nether_star");
    /** The mark of a built-in preset, which can be used and copied but not changed. */
    private static final Icon BUILT_IN = Icon.item("minecraft:book");
    /** The width of a switch, in GUI pixels. */
    private static final int TOGGLE_WIDTH = 2 * BUTTON_WIDTH + GAP;
    /** A preset of the player's own, written in: with a plus, saved as a new one; with a circular arrow, over one. */
    private static final Icon BOOK_TO_WRITE = Icon.item("minecraft:writable_book");
    /** The built-ins setting's states, in its order: all shown, the game's hidden, all hidden. */
    private static final List<Icon> BUILT_INS = List.of(BUILT_IN, Icon.struck(Icon.item("minecraft:grass_block")),
            Icon.struck(BUILT_IN));
    /** What the player is told when a preset could not be written. */
    private static final Component NOT_SAVED = Component.translatable("flatgen.library.not_saved");
    /** The name a new preset starts with, numbered when taken. */
    private static final String NEW_NAME = "Preset";

    private final EditorContext editor;
    private final PresetStore store;
    private Choices choices;
    private List<PresetLibrary.Entry> entries = List.of();
    /** The parts each preset names that the game lacks, by file, worked out as the presets are read. */
    private final Map<String, MissingIds> missing = new HashMap<>();
    /** The file of the card picked; none before one is. */
    private Optional<String> picked = Optional.empty();
    private String name = "";
    private TextField nameField;
    /** What the last share, paste or import said; nothing before one. */
    private Component message = Component.empty();

    /** The library of the player's presets, for the editor that opened it. */
    public LibraryScreen(EditorContext editor) {
        super(Component.translatable("flatgen.library.title"));
        this.editor = editor;
        this.store = PresetStore.player();
        this.choices = ChoicesFile.read();
        reload();
        this.name = PresetLibrary.newName(NEW_NAME, names());
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        FlowLayout rows = new FlowLayout(new Rect(MARGIN, ROWS_TOP, width - 2 * MARGIN, 0), ROW_HEIGHT, GAP);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new Label(Icon.item("minecraft:name_tag"), Component.empty(), rows.next(Icon.SIZE)));
        nameField = new TextField("name", rows.next(NAME_WIDTH), Component.translatable("flatgen.library.name"), name,
                text -> name = text);
        widgets.add(nameField);
        widgets.add(picture(Icon.badged(BOOK_TO_WRITE, "badge_plus"), "flatgen.library.save_new", rows, this::saveNew,
                () -> !name.isBlank()));
        widgets.add(picture(Icon.item("minecraft:anvil"), "flatgen.library.rename", rows, this::rename,
                () -> pickedIsPlayers() && pickedPreset().isPresent() && !name.isBlank()));
        widgets.add(new TextButton(Icon.TICK, () -> Component.translatable("flatgen.library.apply"),
                rows.next(BUTTON_WIDTH), this::apply, () -> pickedPreset().isPresent(),
                () -> List.of(Component.translatable("flatgen.library.apply_tip"))));
        widgets.add(new TextButton(Icon.own("back"), Component.translatable("flatgen.library.back"),
                rows.next(BUTTON_WIDTH), this::onClose));
        rows.newRow();
        widgets.add(picture(Icon.badged(this::drawPicked, "badge_copy"), "flatgen.library.duplicate", rows,
                this::duplicate, () -> pickedPreset().isPresent()));
        widgets.add(picture(Icon.TO_CLIPBOARD, "flatgen.library.share", rows, this::share,
                () -> pickedPreset().isPresent()));
        widgets.add(picture(Icon.FROM_CLIPBOARD, "flatgen.library.paste", rows, this::paste, () -> true));
        widgets.add(picture(Icon.own("folder"), "flatgen.library.folder", rows,
                () -> SystemFiles.openFolder(store.folder()), () -> true));
        widgets.add(new CycleButton(BUILT_INS, () -> choices.builtIns().ordinal(),
                rows.next(CycleButton.width(BUILT_INS.size())), () -> choose(choices.nextBuiltIns()),
                () -> List.of(Component.translatable("flatgen.library.built_ins."
                        + choices.builtIns().name().toLowerCase(Locale.ROOT)),
                        Component.translatable("flatgen.library.built_ins_tip"))));
        rows.next(DANGER_GAP);
        widgets.add(picture(Icon.badged(BOOK_TO_WRITE, "badge_swap"), "flatgen.library.overwrite", rows,
                this::overwrite, this::pickedIsPlayers));
        widgets.add(picture(Icon.own("trash"), "flatgen.library.delete", rows, this::delete, this::pickedIsPlayers));
        rows.newRow();
        widgets.add(new Toggle(DEFAULT, Component.translatable("flatgen.library.default"), rows.next(TOGGLE_WIDTH),
                () -> picked.isPresent() && picked.equals(choices.defaultFile()),
                () -> choose(choices.toggleDefault(picked.get())), () -> pickedPreset().isPresent(),
                () -> List.of(Component.translatable("flatgen.library.default_tip"))));
        widgets.add(new Toggle(Icon.item("minecraft:grass_block"), Component.translatable("flatgen.library.start"),
                rows.next(TOGGLE_WIDTH), choices::startOnSuperflat, () -> choose(choices.toggleStartOnSuperflat()),
                () -> true, () -> List.of(Component.translatable("flatgen.library.start_tip"))));
        widgets.add(new TextLines(rows.line(font.lineHeight), () -> List.of(message)));
        int gridTop = rows.bottom() + GAP;
        widgets.add(new CardGrid(new Rect(MARGIN, gridTop, width - 2 * MARGIN, height - gridTop - MARGIN),
                CARD_WIDTH, CARD_HEIGHT, this::sections, choices::isFavourite, new Cards(), this::pick,
                file -> choose(choices.toggleFavourite(file))));
        return widgets;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(editor.screen());
    }

    /** A picture button, its tooltip its name and what it does. */
    private static PictureButton picture(Icon icon, String key, FlowLayout rows, Runnable action,
            BooleanSupplier usable) {
        return new PictureButton(icon, rows.next(PictureButton.SIZE), action, usable,
                () -> List.of(Component.translatable(key), Component.translatable(key + "_tip")));
    }

    /** Draws the picked preset's picture; nothing before one is picked. */
    private void drawPicked(Canvas canvas, int x, int y) {
        pickedPreset().ifPresent(preset -> canvas.itemIcon(preset.icon(), x, y));
    }

    private List<CardGrid.Section> sections() {
        return List.of(new CardGrid.Section(Component.translatable("flatgen.library.presets"), entries.stream()
                .map(entry -> new Search.Entry(entry.key(), entry.name(), "")).toList()));
    }

    private void pick(String file) {
        picked = Optional.of(file);
        entry(file).ifPresent(entry -> nameField.show(entry.name()));
    }

    private void saveNew() {
        saved(store.saveNew(editor.shown().preset().withTopBlockIcon().withName(name.strip())))
                .ifPresent(this::reloadPicking);
    }

    private void rename() {
        String file = picked.get();
        saved(store.rename(file, pickedPreset().orElseThrow(), name.strip()))
                .ifPresent(renamed -> renamedTo(file, renamed));
    }

    /** The choices follow a preset whose file was renamed, and it stays picked. */
    private void renamedTo(String file, String renamed) {
        choose(choices.renamed(file, renamed));
        reloadPicking(renamed);
    }

    private void duplicate() {
        saved(store.duplicate(pickedPreset().orElseThrow())).ifPresent(this::reloadPicking);
    }

    /** The file a preset was written to, passed on; when it could not be written, the player is told. */
    private Optional<String> saved(Optional<String> file) {
        if (file.isEmpty()) {
            message = NOT_SAVED;
        }
        return file;
    }

    private void overwrite() {
        String file = picked.get();
        String shownName = entry(file).map(PresetLibrary.Entry::name).orElse(file);
        ask("flatgen.library.overwrite_question", shownName, () -> overwritten(file, shownName));
    }

    /** Saves the editor's preset over a file, under its name; when it could not be written, the player is told. */
    private void overwritten(String file, String shownName) {
        if (!store.overwrite(file, editor.shown().preset().withTopBlockIcon().withName(shownName))) {
            message = NOT_SAVED;
        }
    }

    private void delete() {
        String file = picked.get();
        ask("flatgen.library.delete_question", entry(file).map(PresetLibrary.Entry::name).orElse(file),
                () -> deleted(file));
    }

    /** Uses the preset picked in the editor, asking first about parts the game lacks; it counts as used lately. */
    private void apply() {
        Preset preset = pickedPreset().orElseThrow();
        choose(choices.used(picked.get()));
        PresetUse.use(editor, this, preset);
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

    @Override
    protected void filesDropped(List<Path> files) {
        said(PresetImports.fromFiles(files, store));
    }

    /** Puts the picked preset on the clipboard as its file's text, to paste into another game or a message. */
    private void share() {
        pickedPreset().ifPresent(preset -> Clipboard.set(PresetJson.write(preset)));
        message = Component.translatable("flatgen.library.shared");
    }

    /** Adds the preset or superflat code on the clipboard to the player's presets. */
    private void paste() {
        said(PresetImports.fromText(Clipboard.get(), store));
    }

    /** Shows what an import did, and the presets as they are now. */
    private void said(PresetImports.Outcome outcome) {
        message = ImportSummary.of(outcome);
        reload();
    }

    private void deleted(String file) {
        if (store.delete(file)) {
            choose(choices.deleted(file));
        } else {
            message = Component.translatable("flatgen.library.not_deleted");
        }
    }

    /** Keeps the player's choices, and shows the presets in their order. */
    private void choose(Choices chosen) {
        choices = chosen;
        ChoicesFile.save(chosen);
        reload();
    }

    private void reload() {
        entries = PresetLibrary.ordered(PresetLibrary.shown(LibrarySources.entries(editor.registries()),
                choices.builtIns()), Set.copyOf(choices.picks().starred()), choices.picks().recent());
        missing.clear();
        entries.forEach(entry -> entry.preset().ifPresent(preset -> missing.put(entry.key(),
                FlatSettings.missingIds(preset, editor.registries()))));
    }

    private Optional<PresetLibrary.Entry> entry(String file) {
        return entries.stream().filter(entry -> entry.key().equals(file)).findFirst();
    }

    /** Whether the preset picked is the player's own, which they may rename, save over and delete. */
    private boolean pickedIsPlayers() {
        return picked.flatMap(this::entry).filter(PresetLibrary.Entry::isPlayers).isPresent();
    }

    private Optional<Preset> pickedPreset() {
        return picked.flatMap(this::entry).flatMap(PresetLibrary.Entry::preset);
    }

    private List<String> names() {
        return entries.stream().map(PresetLibrary.Entry::name).toList();
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
            preset.ifPresent(found -> canvas.itemIcon(found.icon(), x + PICTURE_WIDTH + PADDING, y));
            int markX = x + PICTURE_WIDTH + Icon.SIZE + 2 * PADDING;
            if (choices.defaultFile().filter(card.id()::equals).isPresent()) {
                DEFAULT.draw(canvas, markX, y);
            }
            if (entry.filter(found -> !found.isPlayers()).isPresent()) {
                BUILT_IN.draw(canvas, markX, y + Icon.SIZE + PADDING);
            }
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
                lines.add(Component.translatable("flatgen.library.missing", PresetUse.listed(lacking)));
            }
            return lines;
        }
    }
}
