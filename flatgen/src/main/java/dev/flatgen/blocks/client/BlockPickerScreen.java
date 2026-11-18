package dev.flatgen.blocks.client;

import dev.flatgen.blocks.BlockPicksFile;
import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextField;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.RowLayout;
import dev.flatgen.mc.BlockCatalog;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.search.rules.PickSections;
import dev.flatgen.search.rules.Picks;
import dev.flatgen.search.rules.Search;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;

/**
 * The block picker: every block the world allows as icons, the starred and recent ones first, found by typing and
 * narrowed to one mod. Picking a block hands it on and goes back; the picks and stars are kept for next time.
 */
public final class BlockPickerScreen extends FlatgenScreen {
    /** Space above the grid for the title and the search row, in GUI pixels. */
    private static final int HEADER = 48;
    /** Space below the grid for Cancel, in GUI pixels. */
    private static final int FOOTER = 32;
    private static final int MARGIN = 12;
    private static final int ROW_TOP = 22;
    private static final int ROW_HEIGHT = 20;
    private static final int GAP = 8;
    private static final int SEARCH_WIDTH = 160;
    private static final int MOD_WIDTH = 140;
    private static final int CANCEL_WIDTH = 100;

    private final Screen parent;
    private final Consumer<String> onPick;
    private final List<Search.Entry> blocks;
    private final List<String> mods;
    private Picks picks;
    private String query = "";
    /** The mod the grid is narrowed to, by its place in {@link #mods}; -1 for every mod. */
    private int mod = -1;
    private BlockGrid grid;
    /** The sections for {@link #sectionsFor}, kept until the search, the mod or the picks changes. */
    private List<BlockGrid.Section> sections;
    private List<Object> sectionsFor;

    /** A picker over the blocks these features allow, handing a picked block to {@code onPick}. */
    public BlockPickerScreen(Screen parent, FeatureFlagSet enabled, Consumer<String> onPick) {
        super(Component.translatable("flatgen.blocks.title"));
        this.parent = parent;
        this.onPick = onPick;
        this.blocks = BlockCatalog.blocks(enabled);
        this.mods = blocks.stream().map(Search.Entry::namespace).distinct().sorted().toList();
        this.picks = BlockPicksFile.read();
    }

    /** The grid, as last laid out. */
    public BlockGrid grid() {
        return grid;
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        RowLayout row = new RowLayout(MARGIN, ROW_TOP, ROW_HEIGHT, GAP);
        grid = new BlockGrid(new Rect(MARGIN, HEADER, width - 2 * MARGIN, height - HEADER - FOOTER), this::sections,
                id -> picks.starred().contains(id), this::modName, this::pick, this::star);
        return List.of(
                new TextField("search", row.next(SEARCH_WIDTH), Component.translatable("flatgen.blocks.search"),
                        query, this::search),
                new TextButton(this::modLabel, row.next(MOD_WIDTH), this::nextMod, () -> true, List::of),
                grid,
                new TextButton(CommonComponents.GUI_CANCEL,
                        new Rect((width - CANCEL_WIDTH) / 2, height - (FOOTER + ROW_HEIGHT) / 2, CANCEL_WIDTH,
                                ROW_HEIGHT), this::onClose));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** What the grid shows, worked out again only when the search, the mod or the picks changed. */
    private List<BlockGrid.Section> sections() {
        List<Object> key = List.of(query, mod, picks);
        if (!key.equals(sectionsFor)) {
            sectionsFor = key;
            sections = workOutSections();
        }
        return sections;
    }

    /** What the grid shows: what a search found, or the starred, the recent and then every block of the mod. */
    private List<BlockGrid.Section> workOutSections() {
        List<Search.Entry> shown = mod < 0 ? blocks
                : blocks.stream().filter(block -> block.namespace().equals(mods.get(mod))).toList();
        return PickSections.of(blocks, shown, query, picks).stream()
                .map(section -> new BlockGrid.Section(heading(section.kind()), section.entries())).toList();
    }

    private static Component heading(PickSections.Kind kind) {
        return Component.translatable("flatgen.picks." + kind.name().toLowerCase(Locale.ROOT));
    }

    private void pick(String block) {
        picks = picks.used(block);
        BlockPicksFile.save(picks);
        onPick.accept(block);
        onClose();
    }

    private void star(String block) {
        picks = picks.toggleStar(block);
        BlockPicksFile.save(picks);
    }

    private void search(String text) {
        query = text;
        grid.toTop();
    }

    private Component modLabel() {
        return mod < 0 ? Component.translatable("flatgen.blocks.all_mods") : modName(mods.get(mod));
    }

    private void nextMod() {
        mod = mod + 1 < mods.size() ? mod + 1 : -1;
        grid.toTop();
    }

    private Component modName(String namespace) {
        return Component.literal(ClientPlatform.get().modName(namespace));
    }
}
