package dev.flatgen.biomes.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PickerScreen;
import dev.flatgen.facts.rules.Situation;
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
 * The biome picker (F07) over every biome of the world being created, modded and data pack ones included, its picks and
 * stars kept in biomes.json; {@link BiomePickerScreen} is how it looks.
 */
public final class BiomePicker {
    /** config/flatgen/biomes.json, the biome feature's own file: the biomes picked lately and starred. */
    private static final PicksFile PICKS = new PicksFile("biomes.json");

    private BiomePicker() {
    }

    /**
     * The picker over the biomes of these registries, going back to {@code parent}, handing on the biome picked.
     *
     * @param situation the world as the editor has it, but of a biome, for each biome's facts
     * @param now       what the picker says of the dimension's biome now, as "The Overworld's biome now"
     * @param current   that biome, by id
     */
    public static PickerScreen screen(Screen parent, HolderLookup.Provider registries,
            Function<BiomeInfo, Situation> situation, Component now, String current, Consumer<String> onPick) {
        Map<String, BiomeInfo> biomes = Biomes.all(registries).stream()
                .collect(Collectors.toMap(BiomeInfo::id, Function.identity(), (first, second) -> first));
        List<Search.Entry> entries = biomes.values().stream()
                .map(biome -> new Search.Entry(biome.id(), Names.biome(biome.id()).getString(), biome.namespace()))
                .sorted(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)))
                .toList();
        PickerScreen.Picker picker = new PickerScreen.Picker(Component.translatable("flatgen.biomes.title"),
                Component.translatable("flatgen.biomes.search"), entries,
                id -> new BiomeIcon(biomes.get(id)), ClientPlatform.get()::modName, PICKS::read, PICKS::save);
        return new BiomePickerScreen(parent, picker, biomes, situation, now, current, onPick);
    }

    /** A biome's picture, its landscape in its colours, as the biome picker and the editor show it. */
    public static Icon picture(String biome, HolderLookup.Provider registries) {
        return Biomes.info(biome, registries).<Icon>map(BiomeIcon::new).orElse(Icon.MISSING);
    }
}
