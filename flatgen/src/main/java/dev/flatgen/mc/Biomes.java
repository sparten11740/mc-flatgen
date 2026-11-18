package dev.flatgen.mc;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** What Flatgen shows of biomes, from the registries of the world being created. */
public final class Biomes {
    /** The game's own default water colour, RGB: a biome the game lacks has it, and a fluid's icon. */
    public static final int DEFAULT_WATER = 0x3F76E4;

    private Biomes() {
    }

    /** A biome's water colour, RGB. */
    public static int waterColor(String biome, HolderLookup.Provider registries) {
        ResourceLocation location = ResourceLocation.tryParse(biome);
        if (location == null) {
            return DEFAULT_WATER;
        }
        return registries.lookupOrThrow(Registries.BIOME).get(ResourceKey.create(Registries.BIOME, location))
                .map(found -> found.value().getWaterColor()).orElse(DEFAULT_WATER);
    }
}
