package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Names;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.search.PicksFile;
import dev.flatgen.search.rules.Search;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;

/**
 * The biome picker (F07): every biome of the world being created, modded and data pack ones included, as a card with
 * its name and swatches of its grass, leaves, water and sky; picks and stars kept in biomes.json.
 */
public final class BiomePicker {
    /** A card's size, in GUI pixels: three fit across the narrowest screen. */
    private static final int CARD_WIDTH = 100;
    private static final int CARD_HEIGHT = 32;
    /** Space inside a card's edge, in GUI pixels. */
    private static final int PADDING = 3;
    /** A swatch's size and the space after it, in GUI pixels. */
    private static final int SWATCH = 12;
    private static final int SWATCH_GAP = 3;
    /** Space left at a card's right for the grid's star mark, in GUI pixels. */
    private static final int STAR_ROOM = 6;
    private static final int CARD_BACKGROUND = 0x50000000;
    private static final int SWATCH_RIM = 0xFF000000;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    /** config/flatgen/biomes.json, the biome feature's own file: the biomes picked lately and starred. */
    private static final PicksFile PICKS = new PicksFile("biomes.json");

    private BiomePicker() {
    }

    /** The picker over the biomes of these registries, going back to {@code parent}, handing on the biome picked. */
    public static PickerScreen screen(Screen parent, HolderLookup.Provider registries, Consumer<String> onPick) {
        Map<String, BiomeInfo> biomes = Biomes.all(registries).stream()
                .collect(Collectors.toMap(BiomeInfo::id, Function.identity(), (first, second) -> first));
        List<Search.Entry> entries = biomes.values().stream()
                .map(biome -> new Search.Entry(biome.id(), Names.biome(biome.id()).getString(), biome.namespace()))
                .sorted(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)))
                .toList();
        return new PickerScreen(parent, new PickerScreen.Picker(Component.translatable("flatgen.biomes.title"),
                Component.translatable("flatgen.biomes.search"), Component.translatable("flatgen.biomes.all"),
                entries, CARD_WIDTH, CARD_HEIGHT, new Cards(biomes), ClientPlatform.get()::modName, PICKS::read,
                PICKS::save), onPick);
    }

    /** A biome's card: its name over swatches of its colours. */
    private record Cards(Map<String, BiomeInfo> biomes) implements CardGrid.Cards {
        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            canvas.fill(new Rect(place.x() + 1, place.y() + 1, place.width() - 2, place.height() - 2),
                    CARD_BACKGROUND);
            canvas.clip(new Rect(place.x(), place.y(), place.width() - STAR_ROOM, place.height()));
            canvas.text(Component.literal(entry.name()), place.x() + PADDING, place.y() + PADDING, NAME_COLOR);
            canvas.unclip();
            List<Integer> colours = biomes.get(entry.id()).colours().swatches();
            int y = place.bottom() - PADDING - SWATCH;
            for (int index = 0; index < colours.size(); index++) {
                Rect swatch = new Rect(place.x() + PADDING + index * (SWATCH + SWATCH_GAP), y, SWATCH, SWATCH);
                canvas.fill(swatch, colours.get(index));
                canvas.outline(swatch, SWATCH_RIM);
            }
        }
    }
}
