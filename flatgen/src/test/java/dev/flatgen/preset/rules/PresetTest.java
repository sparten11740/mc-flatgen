package dev.flatgen.preset.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** A preset's own rules: its height, what fits a world, and that nothing changes it from outside. */
class PresetTest {
    @Test
    void addsUpItsLayersHeights() {
        assertEquals(104, TestPresets.TALL_FLAT.height());
    }

    @Test
    void keepsTheNetherAndTheEndThroughEveryChangeAndDropsOneWhenTold() {
        Stack nether = new Stack(List.of(new Layer("minecraft:netherrack", 3)), "minecraft:nether_wastes");
        Preset withNether = TestPresets.TALL_FLAT.withStack(Dimension.NETHER, Optional.of(nether));
        Preset changed = withNether.withName("Other").withIcon("minecraft:stone").withBiome("minecraft:desert")
                .withLayers(List.of(new Layer("minecraft:stone", 1))).withOptions(Map.of("flatgen:always_day", true))
                .withStructureSets(Optional.of(List.of())).withDecorations(true, true);
        assertEquals(Optional.of(nether), changed.stack(Dimension.NETHER));
        assertEquals(Optional.empty(), changed.stack(Dimension.END));
        assertEquals(Optional.empty(), withNether.withStack(Dimension.NETHER, Optional.empty())
                .stack(Dimension.NETHER));
        assertEquals(new Stack(List.of(new Layer("minecraft:stone", 1)), "minecraft:desert"),
                changed.stack(Dimension.OVERWORLD).orElseThrow());
    }

    @Test
    void takesTheNetherAndTheEndFromElsewhereKeepingItsOverworld() {
        Stack end = new Stack(List.of(new Layer("minecraft:end_stone", 2)), "minecraft:the_end");
        Preset other = TestPresets.TALL_FLAT.withStack(Dimension.END, Optional.of(end));
        Preset taken = new Preset("Other", "", List.of(new Layer("minecraft:sand", 1)), "minecraft:desert",
                Optional.empty(), false, false, Map.of()).withOtherStacks(other::stack);
        assertEquals(Optional.of(end), taken.stack(Dimension.END));
        assertEquals(Optional.empty(), taken.stack(Dimension.NETHER));
        assertEquals(List.of(new Layer("minecraft:sand", 1)), taken.layers());
    }

    @Test
    void takesItsTopLayersBlockAsIconWhenItHasLayers() {
        assertEquals("minecraft:grass_block", TestPresets.TALL_FLAT.withIcon("").withTopBlockIcon().icon());
        Preset empty = TestPresets.TALL_FLAT.withLayers(List.of()).withIcon("minecraft:glass");
        assertEquals("minecraft:glass", empty.withTopBlockIcon().icon());
    }

    @Test
    void refusesALayerLessThanOneBlockDeep() {
        assertThrows(IllegalArgumentException.class, () -> new Layer("minecraft:stone", 0));
    }

    @Test
    void keepsItsOwnCopiesOfWhatItIsGiven() {
        List<Layer> layers = new ArrayList<>(List.of(new Layer("minecraft:stone", 1)));
        Map<String, Boolean> options = new LinkedHashMap<>(Map.of("flatgen:always_day", true));
        Preset preset = new Preset("", "minecraft:stone", layers, Preset.DEFAULT_BIOME, Optional.empty(), false, false,
                options);
        layers.add(new Layer("minecraft:dirt", 1));
        options.put("flatgen:always_clear", true);
        assertEquals(1, preset.layers().size());
        assertEquals(Map.of("flatgen:always_day", true), preset.options());
        assertThrows(UnsupportedOperationException.class, () -> preset.layers().add(new Layer("minecraft:dirt", 1)));
    }

    @Test
    void ordersItsOptionsById() {
        Map<String, Boolean> options = new LinkedHashMap<>();
        options.put("flatgen:no_mobs", true);
        options.put("flatgen:always_day", true);
        Preset preset = new Preset("", "", List.of(), Preset.DEFAULT_BIOME, Optional.empty(), false, false, options);
        assertEquals(List.of("flatgen:always_day", "flatgen:no_mobs"), new ArrayList<>(preset.options().keySet()));
    }
}
