package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.CardGrid;
import dev.flatgen.client.gui.GridPickerScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.facts.client.FactPictures;
import dev.flatgen.facts.rules.Fact;
import dev.flatgen.facts.rules.Facts;
import dev.flatgen.facts.rules.Situation;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Names;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.search.PicksFile;
import dev.flatgen.search.rules.Search;
import java.util.Comparator;
import java.util.HashMap;
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
 * its name, swatches of its grass, leaves, water and sky, and the pictures of the facts that differ from biome to biome
 * (F08) for the world as the editor has it; picks and stars kept in biomes.json.
 */
public final class BiomePicker {
    /** A card's size, in GUI pixels: two fit across the narrowest screen, three across the dev client's. */
    private static final int CARD_WIDTH = 128;
    private static final int CARD_HEIGHT = 36;
    /** Space between the swatches and the fact pictures, and between pictures, in GUI pixels. */
    private static final int PICTURE_GAP = 4;
    private static final int FACT_GAP = 1;
    /** Space inside a card's edge, in GUI pixels. */
    private static final int PADDING = 3;
    /** A swatch's size and the space after it, in GUI pixels. */
    private static final int SWATCH = 12;
    private static final int SWATCH_GAP = 3;
    /** Space left at a card's right for the grid's star mark, in GUI pixels. */
    private static final int STAR_ROOM = 8;
    private static final int CARD_BACKGROUND = 0x50000000;
    private static final int SWATCH_RIM = 0xFF000000;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    /** config/flatgen/biomes.json, the biome feature's own file: the biomes picked lately and starred. */
    private static final PicksFile PICKS = new PicksFile("biomes.json");

    private BiomePicker() {
    }

    /**
     * The picker over the biomes of these registries, going back to {@code parent}, handing on the biome picked.
     *
     * @param situation the world as the editor has it, but of a biome, for each card's facts
     */
    public static PickerScreen screen(Screen parent, HolderLookup.Provider registries,
            Function<BiomeInfo, Situation> situation, Consumer<String> onPick) {
        Map<String, BiomeInfo> biomes = Biomes.all(registries).stream()
                .collect(Collectors.toMap(BiomeInfo::id, Function.identity(), (first, second) -> first));
        List<Search.Entry> entries = biomes.values().stream()
                .map(biome -> new Search.Entry(biome.id(), Names.biome(biome.id()).getString(), biome.namespace()))
                .sorted(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)))
                .toList();
        return new GridPickerScreen(parent, new PickerScreen.Picker(Component.translatable("flatgen.biomes.title"),
                Component.translatable("flatgen.biomes.search"), entries,
                id -> new BiomeIcon(biomes.get(id).colours()), ClientPlatform.get()::modName, PICKS::read,
                PICKS::save), Component.translatable("flatgen.biomes.all"), CARD_WIDTH, CARD_HEIGHT,
                new Cards(biomes, situation), onPick);
    }

    /**
     * A biome's card: its name over swatches of its colours and the pictures of its facts, worked out once per biome
     * while the picker is open; their words are its tooltip.
     */
    private static final class Cards implements CardGrid.Cards {
        private final Map<String, BiomeInfo> biomes;
        private final Function<BiomeInfo, Situation> situation;
        private final Map<String, List<Fact.Shown>> facts = new HashMap<>();

        Cards(Map<String, BiomeInfo> biomes, Function<BiomeInfo, Situation> situation) {
            this.biomes = biomes;
            this.situation = situation;
        }

        @Override
        public void draw(Canvas canvas, Search.Entry entry, Rect place) {
            canvas.fill(new Rect(place.x() + 1, place.y() + 1, place.width() - 2, place.height() - 2),
                    CARD_BACKGROUND);
            canvas.clip(new Rect(place.x(), place.y(), place.width() - STAR_ROOM, place.height()));
            canvas.text(Component.literal(entry.name()), place.x() + PADDING, place.y() + PADDING, NAME_COLOR);
            canvas.unclip();
            List<Integer> colours = biomes.get(entry.id()).colours().swatches();
            int rowY = place.bottom() - PADDING - Icon.SIZE;
            int x = place.x() + PADDING;
            for (Integer colour : colours) {
                Rect swatch = new Rect(x, rowY + (Icon.SIZE - SWATCH) / 2, SWATCH, SWATCH);
                canvas.fill(swatch, colour);
                canvas.outline(swatch, SWATCH_RIM);
                x += SWATCH + SWATCH_GAP;
            }
            x += PICTURE_GAP;
            for (Fact.Shown shown : facts(entry)) {
                FactPictures.icon(shown).draw(canvas, x, rowY);
                x += Icon.SIZE + FACT_GAP;
            }
        }

        @Override
        public List<Component> describe(Search.Entry entry) {
            return facts(entry).stream().flatMap(shown -> FactPictures.words(shown).stream()).toList();
        }

        private List<Fact.Shown> facts(Search.Entry entry) {
            return facts.computeIfAbsent(entry.id(), id -> show(situation.apply(biomes.get(id))));
        }

        private static List<Fact.Shown> show(Situation world) {
            return Facts.ABOUT_THE_BIOME.stream().map(fact -> fact.show(world)).toList();
        }
    }
}
