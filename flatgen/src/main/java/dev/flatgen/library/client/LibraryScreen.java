package dev.flatgen.library.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.MenuItem;
import dev.flatgen.client.gui.MenuScreen;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.QuestionScreen;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextField;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.PageFrame;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.exports.PresetExports;
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
import dev.flatgen.mc.Screens;
import dev.flatgen.mc.SystemFiles;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.MissingIds;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.PresetJson;
import dev.flatgen.search.rules.Search;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

/**
 * The preset library (F11), opened from the editor, as round 3 of docs/design/simple-editor.md drew it: a header with
 * Back, a search, saving the editor's preset as a new one and a menu of the rest (pasting, the folder, the exports and
 * the library's settings); the player's presets and the built-in ones as cards, each its layers standing in the sky
 * over its name (a barrier on one naming parts the game lacks or that cannot be read, a bed on the default); and a
 * footer for the card picked: its name to change, renaming, copying, sharing, the default, saving over, deleting and
 * Use (a step to undo). Saving over, deleting and using one with missing parts ask first.
 */
public final class LibraryScreen extends FlatgenScreen {
    /** Space between the header's and the footer's parts, in GUI pixels. */
    private static final int GAP = 6;
    /** Widths in GUI pixels: the search field and the field of the picked preset's name. */
    private static final int SEARCH_WIDTH = 130;
    private static final int NAME_WIDTH = 110;
    /** A card's least width and its height, in GUI pixels: eight across the narrowest screen. */
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 80;
    /** The sky on a card, the layers standing in it, and the space above the sky, in GUI pixels. */
    private static final int SKY_WIDTH = 32;
    private static final int SKY_HEIGHT = 40;
    private static final int TOWER_WIDTH = 13;
    private static final int TOWER_HEIGHT = 28;
    private static final int CARD_TOP = 6;
    /** Space between a card and the next, which its panel leaves at each side, in GUI pixels. */
    private static final int CARD_INSET = 2;
    /** Space between a card's sky, name and source, and around a source's words on its chip, in GUI pixels. */
    private static final int CARD_SPACING = 3;
    private static final int CHIP_PADDING = 4;
    /** How much taller a source's chip is than its words, in GUI pixels: its border above and below. */
    private static final int CHIP_BORDER = 2;
    /** The colour of the sky on a card, ARGB: the overworld's. */
    private static final int SKY = 0xFF9CC3F7;
    /** The picked preset's layers in the footer, in GUI pixels. */
    private static final int PICKED_WIDTH = 7;
    private static final int PICKED_HEIGHT = 16;
    /** The mark of the default preset, the one Superflat starts with. */
    private static final Icon DEFAULT = Icon.item("minecraft:red_bed");
    /** A book, which stands for the built-in presets in the setting of which of them show. */
    private static final Icon BUILT_IN = Icon.item("minecraft:book");
    /** A preset of the player's own, written in: with a plus, saved as a new one; with a circular arrow, over one. */
    private static final Icon BOOK_TO_WRITE = Icon.item("minecraft:writable_book");
    /** Saving over a preset and deleting one, which cannot be undone, on their buttons and their questions. */
    private static final Icon SAVE_OVER = Icon.badged(BOOK_TO_WRITE, "badge_swap");
    private static final Icon DELETE = Icon.line("trash");
    /** The built-ins setting's states, in its order: all shown, the game's hidden, all hidden. */
    private static final List<Icon> BUILT_INS = List.of(BUILT_IN, Icon.struck(Icon.item("minecraft:grass_block")),
            Icon.struck(BUILT_IN));
    /** What the player is told when a preset could not be written. */
    private static final Component NOT_SAVED = Component.translatable("flatgen.library.not_saved");

    private final EditorContext editor;
    private final PresetStore store;
    private Choices choices;
    private List<PresetLibrary.Entry> entries = List.of();
    /** The grid's sections, and the presets and the search they were made for. */
    private List<CardGrid.Section> sections = List.of();
    private List<Object> sectionsFor;
    /** The parts each preset names that the game lacks, by file, worked out as the presets are read. */
    private final Map<String, MissingIds> missing = new HashMap<>();
    /** The file of the card picked; none before one is. */
    private Optional<String> picked = Optional.empty();
    /** What the picked preset's name field holds. */
    private String name = "";
    private TextField nameField;
    private String query = "";
    private CardGrid grid;
    /** What the last share, paste or import said; nothing before one. */
    private Component message = Component.empty();

    /** The library of the player's presets, for the editor that opened it. */
    public LibraryScreen(EditorContext editor) {
        super(Component.translatable("flatgen.library.title"));
        this.editor = editor;
        this.store = PresetStore.player();
        this.choices = ChoicesFile.read();
        reload();
    }

    /** The header, the cards with what the last action said under them, and the footer of the card picked. */
    @Override
    protected List<Widget> layOut(int width, int height) {
        PageFrame frame = new PageFrame(width, height);
        Rect body = frame.body();
        int line = TextStyle.BODY.lineHeight();
        List<Widget> widgets = new ArrayList<>(header(width));
        grid = new CardGrid(new Rect(body.x(), body.y(), body.width(), body.height() - line - GAP), CARD_WIDTH,
                CARD_HEIGHT, this::sections, id -> choices.isFavourite(id), new Cards(), this::pick,
                file -> choose(choices.toggleFavourite(file)));
        widgets.add(grid);
        widgets.add(new Caption(() -> message, new Rect(body.x(), body.bottom() - line, body.width(), line),
                TextStyle.BODY, Look.MUTED));
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

    /** Back, the title and the search at the left; saving as new and the menu of the rest at the right. */
    private List<Widget> header(int width) {
        int top = (PageFrame.HEADER - PictureButton.SIZE) / 2;
        PictureButton back = backButton();
        int titleX = back.bounds().right() + GAP;
        int titleWidth = textWidth(getTitle(), TextStyle.TITLE);
        int searchX = titleX + titleWidth + 2 * GAP;
        Rect more = new Rect(width - PageFrame.MARGIN - PictureButton.SIZE, top, PictureButton.SIZE,
                PictureButton.SIZE);
        Component saveLabel = Component.translatable("flatgen.library.save_new");
        int saveWidth = TextButton.width(textWidth(saveLabel));
        Rect save = new Rect(more.x() - GAP - saveWidth, top, saveWidth, PictureButton.SIZE);
        int searchWidth = Math.min(TextField.searchWidth(SEARCH_WIDTH), save.x() - 2 * GAP - searchX);
        return List.of(back,
                new Caption(this::getTitle, new Rect(titleX, 0, titleWidth, PageFrame.HEADER), TextStyle.TITLE,
                        Look.TEXT),
                TextField.search(new Rect(searchX, top, searchWidth, PictureButton.SIZE),
                        Component.translatable("flatgen.library.search"), query, this::search),
                new TextButton(Icon.badged(BOOK_TO_WRITE, "badge_plus"), () -> saveLabel, save, this::saveNew,
                        () -> true, () -> List.of(Component.translatable("flatgen.library.save_new_tip"))),
                new PictureButton(Icon.line("more"), more, () -> openMenu(more),
                        () -> List.of(Component.translatable("flatgen.library.more"))));
    }

    /**
     * The card picked, its layers and its name to change, at the left; what can be done with it at the right, Use
     * last, the others showing their pictures alone when their words do not fit.
     */
    private List<Widget> footer(PageFrame frame) {
        Rect row = frame.footerRow();
        int footerTop = frame.height() - PageFrame.FOOTER;
        int line = TextStyle.BODY.lineHeight();
        int pickedTop = footerTop + (PageFrame.FOOTER - line - row.height()) / 2;
        Rect layers = new Rect(row.x(), footerTop + (PageFrame.FOOTER - PICKED_HEIGHT) / 2, PICKED_WIDTH,
                PICKED_HEIGHT);
        Rect field = new Rect(layers.right() + GAP, pickedTop + line, NAME_WIDTH, row.height());
        nameField = new TextField("name", field, Component.translatable("flatgen.library.name"), name,
                text -> name = text);
        List<Widget> widgets = new ArrayList<>(List.of((canvas, mouseX, mouseY) -> drawPickedLayers(canvas, layers),
                new Caption(() -> Component.translatable("flatgen.library.is_picked"), new Rect(field.x(), pickedTop,
                        NAME_WIDTH, line), TextStyle.BODY, Look.MUTED), nameField));
        widgets.addAll(actions(frame, row.right() - field.right() - 2 * GAP));
        return widgets;
    }

    /** What can be done with the card picked, in this much room, Use last. */
    private List<Widget> actions(PageFrame frame, int room) {
        List<Action> actions = List.of(
                new Action(Icon.item("minecraft:anvil"), "flatgen.library.rename", "flatgen.library.rename_tip",
                        this::rename,
                        () -> pickedIsPlayers() && pickedPreset().isPresent() && !name.isBlank()),
                new Action(Icon.badged(this::drawPickedIcon, "badge_copy"), "flatgen.library.duplicate",
                        "flatgen.library.duplicate_tip", this::duplicate, () -> pickedPreset().isPresent()),
                new Action(Icon.TO_CLIPBOARD, "flatgen.library.share", "flatgen.library.share_tip", this::share,
                        () -> pickedPreset().isPresent()),
                new Action(SAVE_OVER, "flatgen.library.overwrite", "flatgen.library.overwrite_tip", this::overwrite,
                        this::pickedIsPlayers),
                new Action(DELETE, "flatgen.library.delete", "flatgen.library.delete_tip", this::delete,
                        this::pickedIsPlayers));
        Component defaultLabel = Component.translatable("flatgen.library.default");
        Component useLabel = Component.translatable("flatgen.library.apply");
        int toggleWidth = Toggle.width(textWidth(defaultLabel));
        int useWidth = actionWidth(useLabel);
        int[] widths = actions.stream().mapToInt(action -> TextButton.width(textWidth(action.label()))).toArray();
        if (IntStream.of(widths).sum() + toggleWidth + useWidth + (actions.size() + 1) * GAP > room) {
            Arrays.fill(widths, PictureButton.SIZE);
        }
        List<Rect> places = frame.actions(IntStream.concat(IntStream.of(widths), IntStream.of(toggleWidth, useWidth))
                .toArray());
        List<Widget> widgets = new ArrayList<>();
        for (int index = 0; index < actions.size(); index++) {
            widgets.add(actions.get(index).button(places.get(index)));
        }
        widgets.add(new Toggle(DEFAULT, defaultLabel, places.get(actions.size()),
                () -> picked.isPresent() && picked.equals(choices.defaultFile()),
                () -> choose(choices.toggleDefault(picked.get())), () -> pickedPreset().isPresent(),
                () -> List.of(Component.translatable("flatgen.library.default_tip"))));
        widgets.add(new TextButton(Icon.line("check"), () -> useLabel, places.get(actions.size() + 1), this::apply,
                () -> pickedPreset().isPresent(), () -> List.of(Component.translatable("flatgen.library.apply_tip")))
                .kind(Look.ButtonKind.PRIMARY));
        return widgets;
    }

    /**
     * One of the footer's buttons for the card picked: its picture, the translation keys of its name and of what it
     * does, which its tooltip says, what it does and whether it can now.
     */
    private record Action(Icon icon, String key, String tipKey, Runnable run, BooleanSupplier usable) {
        Component label() {
            return Component.translatable(key);
        }

        TextButton button(Rect place) {
            return new TextButton(icon, this::label, place, run, usable,
                    () -> List.of(label(), Component.translatable(tipKey)));
        }
    }

    /** Opens the menu of the rest under its button: pasting and the folder, the exports, the library's settings. */
    private void openMenu(Rect button) {
        Screens.show(new MenuScreen(Component.translatable("flatgen.library.more"), this, button,
                List.of(fileRows(), exportRows(), settingRows())));
    }

    /** The menu's rows for adding presets: pasting one, and the folder to drop files in. */
    private List<MenuScreen.Row> fileRows() {
        return List.of(menuAction(Icon.FROM_CLIPBOARD, "flatgen.library.paste", "flatgen.library.paste_tip",
                        this::paste, () -> true),
                menuAction(Icon.own("folder"), "flatgen.library.folder", "flatgen.library.folder_tip",
                        () -> SystemFiles.openFolder(store.folder()), () -> true));
    }

    /** The menu's rows for the picked preset's exports: the server's lines and a datapack. */
    private List<MenuScreen.Row> exportRows() {
        return List.of(menuAction(Icon.own("server"), "flatgen.library.server", "flatgen.library.server_tip",
                        this::server, () -> pickedPreset().isPresent()),
                menuAction(Icon.item("minecraft:bundle"), "flatgen.library.datapack", "flatgen.library.datapack_tip",
                        this::datapack, () -> pickedPreset().isPresent()));
    }

    /** The menu's rows for the library's settings, which keep it open: starting on Superflat, the built-ins shown. */
    private List<MenuScreen.Row> settingRows() {
        return List.of(MenuScreen.staying(this::startSwitch), MenuScreen.staying(this::builtInsRow));
    }

    private Toggle startSwitch(Rect place) {
        return new Toggle(Icon.item("minecraft:grass_block"), Component.translatable("flatgen.library.start"), place,
                () -> choices.startOnSuperflat(), () -> choose(choices.toggleStartOnSuperflat()), () -> true,
                () -> List.of(Component.translatable("flatgen.library.start_tip")));
    }

    /** The built-ins setting: its picture and name as it is now; a click shows the next. */
    private MenuItem builtInsRow(Rect place) {
        return new MenuItem(this::drawBuiltIns, () -> Component.translatable(builtInsName(choices.builtIns())), place,
                () -> choose(choices.nextBuiltIns()), () -> true,
                () -> List.of(Component.translatable("flatgen.library.built_ins_tip")));
    }

    /** A row of the menu that closes it and does something, its tooltip its name and what it does, by their keys. */
    private static MenuScreen.Row menuAction(Icon icon, String key, String tipKey, Runnable action,
            BooleanSupplier usable) {
        return MenuScreen.action(icon, Component.translatable(key), Component.translatable(tipKey), usable, action);
    }

    /** The built-ins setting's name for a state, by its translation key. */
    private static String builtInsName(PresetLibrary.BuiltIns builtIns) {
        return switch (builtIns) {
            case SHOWN -> "flatgen.library.built_ins.shown";
            case NO_GAME -> "flatgen.library.built_ins.no_game";
            case HIDDEN -> "flatgen.library.built_ins.hidden";
        };
    }

    /** Draws the built-ins setting's picture as it is now. */
    private void drawBuiltIns(Canvas canvas, int x, int y) {
        BUILT_INS.get(choices.builtIns().ordinal()).draw(canvas, x, y);
    }

    /** Draws the picked preset's picture; nothing before one is picked. */
    private void drawPickedIcon(Canvas canvas, int x, int y) {
        pickedPreset().ifPresent(preset -> canvas.itemIcon(preset.icon(), x, y));
    }

    /** Draws the picked preset's layers in the footer; nothing before one is picked. */
    private void drawPickedLayers(Canvas canvas, Rect area) {
        pickedPreset().ifPresent(preset -> ColumnPicture.draw(canvas, preset.layers(),
                Biomes.waterColor(preset.biome(), editor.registries()), area));
    }

    /**
     * The grid's sections, the player's own presets, then the built-in ones, each in the library's order, narrowed to
     * what is searched for and left out when empty; made again only when the presets are read again or the search
     * changes, so the grid keeps its place.
     */
    private List<CardGrid.Section> sections() {
        List<Object> key = List.of(entries, query);
        if (!key.equals(sectionsFor)) {
            sectionsFor = key;
            sections = Stream.of(section("flatgen.library.presets", "flatgen.library.presets_where", true),
                    section("flatgen.library.built_in_presets", "flatgen.library.built_in_where", false))
                    .filter(section -> !section.entries().isEmpty()).toList();
        }
        return sections;
    }

    /** The section of the player's own presets, or of the built-in ones, under its heading and where they are from. */
    private CardGrid.Section section(String heading, String detail, boolean players) {
        List<Search.Entry> cards = entries.stream().filter(entry -> entry.isPlayers() == players)
                .map(entry -> new Search.Entry(entry.key(), entry.name(), entry.namespace().orElse(""))).toList();
        return new CardGrid.Section(Component.translatable(heading), Component.translatable(detail),
                query.isBlank() ? cards : Search.find(cards, query));
    }

    private void search(String text) {
        query = text;
        grid.toTop();
    }

    private void pick(String file) {
        picked = Optional.of(file);
        entry(file).ifPresent(entry -> nameField.show(entry.name()));
    }

    /** Saves the editor's preset as a new one under a name of its own, picked so it can be renamed at once. */
    private void saveNew() {
        String newName = PresetLibrary.newName(Component.translatable("flatgen.library.new_name").getString(), names());
        saved(store.saveNew(editor.shown().preset().withTopBlockIcon().withName(newName)))
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
        ask(SAVE_OVER, "flatgen.library.overwrite_question", shownName, () -> overwritten(file, shownName));
    }

    /**
     * Saves the editor's preset over a file, under its name, which stays picked; when it could not be written, the
     * player is told.
     */
    private void overwritten(String file, String shownName) {
        if (!store.overwrite(file, editor.shown().preset().withTopBlockIcon().withName(shownName))) {
            message = NOT_SAVED;
        }
        reloadPicking(file);
    }

    private void delete() {
        String file = picked.get();
        ask(DELETE, "flatgen.library.delete_question", entry(file).map(PresetLibrary.Entry::name).orElse(file),
                () -> deleted(file));
    }

    /** Uses the preset picked in the editor, asking first about parts the game lacks; it counts as used lately. */
    private void apply() {
        Preset preset = pickedPreset().orElseThrow();
        choose(choices.used(picked.get()));
        PresetUse.use(editor, this, preset);
    }

    /** Asks before something that cannot be undone, showing its picture, about a preset of this name. */
    private void ask(Icon picture, String key, String presetName, Runnable yes) {
        Screens.show(new QuestionScreen(this, picture, Component.translatable("flatgen.library.title"),
                List.of(Component.translatable(key, presetName)), yes));
    }

    private void reloadPicking(String file) {
        reload();
        pick(file);
    }

    @Override
    protected void filesDropped(List<Path> files) {
        said(PresetImports.fromFiles(files, store, Imports.unnamed()));
    }

    /** Puts the picked preset on the clipboard as its file's text, to paste into another game or a message. */
    private void share() {
        pickedPreset().ifPresent(preset -> Clipboard.set(PresetJson.write(preset)));
        message = Component.translatable("flatgen.library.shared");
    }

    /**
     * Puts the lines a server's server.properties needs for the picked preset on the clipboard (F21), asking first
     * when it names parts this game lacks; the message says when the lines leave a flat Nether or End behind.
     */
    private void server() {
        pickedPreset().ifPresent(preset -> PresetUse.madeHere(editor, this, preset, this::copyServerLines));
    }

    private void copyServerLines(Preset preset) {
        Clipboard.set(PresetExports.serverLines(preset, editor.registries(), editor.worldOptions()));
        boolean othersLeft = Dimension.OTHERS.stream().anyMatch(dimension -> preset.stack(dimension).isPresent());
        message = Component.translatable(othersLeft ? "flatgen.library.server_copied_overworld"
                : "flatgen.library.server_copied");
    }

    /**
     * Saves the picked preset as a datapack in the exports folder, which opens to show it (F21), asking first when it
     * names parts this game lacks.
     */
    private void datapack() {
        Optional<String> name = picked.flatMap(this::entry).map(LibraryScreen::exportName);
        pickedPreset().ifPresent(preset -> PresetUse.madeHere(editor, this, preset,
                made -> saveDatapack(made, name.orElseThrow())));
    }

    private void saveDatapack(Preset preset, String exportName) {
        Optional<Path> written = PresetExports.datapack(preset, exportName, editor.registries(),
                editor.worldOptions());
        message = written.<Component>map(file -> Component.translatable("flatgen.library.datapack_saved",
                file.getFileName().toString())).orElse(Component.translatable("flatgen.library.datapack_not_saved"));
        written.ifPresent(file -> SystemFiles.openFolder(PresetExports.folder()));
    }

    /**
     * A datapack's name for a library entry: a player's preset's file name without the ending, as {@code glass_2},
     * or a built-in's name made into one, letters, digits and underscores only.
     */
    private static String exportName(PresetLibrary.Entry entry) {
        String base = entry.isPlayers() && entry.key().endsWith(PresetLibrary.FILE_ENDING)
                ? entry.key().substring(0, entry.key().length() - PresetLibrary.FILE_ENDING.length())
                : entry.name();
        String file = PresetLibrary.fileName(base, List.of());
        return file.substring(0, file.length() - PresetLibrary.FILE_ENDING.length());
    }

    /** Adds the preset or superflat code on the clipboard to the player's presets. */
    private void paste() {
        said(PresetImports.fromText(Clipboard.get(), store, Imports.unnamed()));
    }

    /** Shows what an import did, and the presets as they are now. */
    private void said(PresetImports.Outcome outcome) {
        message = Imports.summary(outcome);
        reload();
    }

    /** Deletes a preset's file, leaving none picked and the name field empty; when it cannot, the player is told. */
    private void deleted(String file) {
        if (!store.delete(file)) {
            message = Component.translatable("flatgen.library.not_deleted");
            return;
        }
        picked = Optional.empty();
        name = "";
        choose(choices.deleted(file));
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

    /**
     * A preset's card: its layers standing in the sky, a bed on the default and a barrier when it is broken or names
     * missing parts, its name, and for a built-in the mod or pack it comes from; its tooltip, and so the narrator, says
     * in words what its marks show: picked, the default, built in.
     */
    private final class Cards implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry card, Rect cell) {
            Rect place = new Rect(cell.x() + CARD_INSET, cell.y() + CARD_INSET, cell.width() - 2 * CARD_INSET,
                    cell.height() - 2 * CARD_INSET);
            canvas.sprite(picked.filter(card.id()::equals).isPresent() ? Look.CHOSEN : Look.PANEL, place);
            Optional<PresetLibrary.Entry> entry = entry(card.id());
            Optional<Preset> preset = entry.flatMap(PresetLibrary.Entry::preset);
            Rect sky = new Rect(place.x() + (place.width() - SKY_WIDTH) / 2, place.y() + CARD_TOP, SKY_WIDTH,
                    SKY_HEIGHT);
            canvas.fill(sky, SKY);
            Rect tower = new Rect(sky.x() + (SKY_WIDTH - TOWER_WIDTH) / 2, sky.bottom() - TOWER_HEIGHT, TOWER_WIDTH,
                    TOWER_HEIGHT);
            preset.ifPresent(found -> ColumnPicture.draw(canvas, found.layers(),
                    Biomes.waterColor(found.biome(), editor.registries()), tower));
            drawMarks(canvas, card, preset, place, sky);
            Rect name = new Rect(place.x() + CARD_SPACING, sky.bottom() + CARD_SPACING,
                    place.width() - 2 * CARD_SPACING, TextStyle.BODY.lineHeight());
            canvas.centeredText(canvas.fitted(Component.literal(card.name()), name.width(), TextStyle.BODY), name,
                    Look.TEXT);
            if (!card.namespace().isEmpty()) {
                drawSource(canvas, Component.literal(ClientPlatform.get().modName(card.namespace())), place,
                        name.bottom() + CARD_SPACING);
            }
        }

        /** A bed on the default preset's card, at its top left; a barrier on the sky of one broken or lacking parts. */
        private void drawMarks(Canvas canvas, Search.Entry card, Optional<Preset> preset, Rect place, Rect sky) {
            if (choices.defaultFile().filter(card.id()::equals).isPresent()) {
                DEFAULT.draw(canvas, place.x() + CARD_SPACING, place.y() + CARD_SPACING);
            }
            if (preset.isEmpty() || !missing.getOrDefault(card.id(), MissingIds.NONE).isEmpty()) {
                Icon.MISSING.draw(canvas, sky.x() + (SKY_WIDTH - Icon.SIZE) / 2, sky.y() + CARD_SPACING);
            }
        }

        /** The mod or pack a built-in comes from, on a chip in the middle of the card from this height. */
        private void drawSource(Canvas canvas, Component source, Rect place, int top) {
            int most = place.width() - 2 * CARD_SPACING;
            Component fitted = canvas.fitted(source, most - 2 * CHIP_PADDING, TextStyle.BODY);
            int chipWidth = Math.min(most, canvas.textWidth(fitted, TextStyle.BODY) + 2 * CHIP_PADDING);
            Rect chip = new Rect(place.x() + (place.width() - chipWidth) / 2, top, chipWidth,
                    TextStyle.BODY.lineHeight() + CHIP_BORDER);
            canvas.sprite(Look.CHIP, chip);
            canvas.centeredText(fitted, chip, Look.MUTED);
        }

        @Override
        public List<Component> describe(Search.Entry card) {
            List<Component> lines = new ArrayList<>();
            if (picked.filter(card.id()::equals).isPresent()) {
                lines.add(Component.translatable("flatgen.library.is_picked"));
            }
            if (choices.defaultFile().filter(card.id()::equals).isPresent()) {
                lines.add(Component.translatable("flatgen.library.is_default"));
            }
            if (entry(card.id()).filter(entry -> !entry.isPlayers()).isPresent()) {
                lines.add(Component.translatable("flatgen.library.is_built_in"));
            }
            if (entry(card.id()).filter(entry -> !entry.problems().isEmpty()).isPresent()) {
                lines.add(Component.translatable("flatgen.library.unreadable"));
            }
            MissingIds lacking = missing.getOrDefault(card.id(), MissingIds.NONE);
            if (!lacking.isEmpty()) {
                lines.add(Component.translatable("flatgen.library.missing", PresetUse.listed(lacking)));
            }
            return lines;
        }
    }
}
