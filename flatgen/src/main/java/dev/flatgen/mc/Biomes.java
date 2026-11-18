package dev.flatgen.mc;

import dev.flatgen.biomes.rules.BiomeInfo;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** What Flatgen shows of biomes, from the registries of the world being created. */
public final class Biomes {
    /** The game's own default water colour, RGB: a biome the game lacks has it, and a fluid's icon. */
    public static final int DEFAULT_WATER = 0x3F76E4;

    private Biomes() {
    }

    /** Every biome of these registries, the game's, mods' and data packs', with its colours. */
    public static List<BiomeInfo> all(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.BIOME).listElements()
                .map(biome -> info(biome.key().location().toString(), biome.value())).toList();
    }

    private static BiomeInfo info(String id, Biome biome) {
        return new BiomeInfo(id, biome.getGrassColor(0, 0), biome.getFoliageColor(), biome.getWaterColor(),
                biome.getSkyColor());
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
