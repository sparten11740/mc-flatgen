package dev.flatgen.preset.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** A preset's own rules: its height, what fits a world, and that nothing changes it from outside. */
class PresetTest {
    /** The stack most of the test instance's superflat worlds use. */
    static final Preset TALL_FLAT = new Preset("Tall flat", "minecraft:grass_block", List.of(
            new Layer("minecraft:bedrock", 1),
            new Layer("minecraft:stone", 100),
            new Layer("minecraft:dirt", 2),
            new Layer("minecraft:grass_block", 1)),
            "minecraft:plains", Optional.of(List.of("minecraft:strongholds", "minecraft:villages")), false, false,
            Map.of("flatgen:always_day", true));

    @Test
    void addsUpItsLayersHeights() {
        assertEquals(104, TALL_FLAT.height());
    }

    @Test
    void fitsAWorldAsTallAsItOrTaller() {
        assertTrue(TALL_FLAT.fitsWorld(104));
        assertTrue(TALL_FLAT.fitsWorld(384));
        assertFalse(TALL_FLAT.fitsWorld(103));
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
