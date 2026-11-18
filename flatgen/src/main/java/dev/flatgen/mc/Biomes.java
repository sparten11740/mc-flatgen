package dev.flatgen.mc;

import dev.flatgen.biomes.rules.BiomeInfo;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

/** What Flatgen shows of biomes, from the registries of the world being created. */
public final class Biomes {
    /** The game's own default water colour, RGB: a biome the game lacks has it, and a fluid's icon. */
    public static final int DEFAULT_WATER = 0x3F76E4;

    private Biomes() {
    }

    /** Every biome of these registries, the game's, mods' and data packs', as Flatgen shows it. */
    public static List<BiomeInfo> all(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.BIOME).listElements().map(Biomes::info).toList();
    }

    /** One biome of these registries, as Flatgen shows it; none for a biome they lack. */
    public static Optional<BiomeInfo> info(String biome, HolderLookup.Provider registries) {
        return Optional.ofNullable(ResourceLocation.tryParse(biome))
                .flatMap(location -> registries.lookupOrThrow(Registries.BIOME)
                        .get(ResourceKey.create(Registries.BIOME, location)))
                .map(Biomes::info);
    }

    private static BiomeInfo info(Holder<Biome> holder) {
        Biome biome = holder.value();
        List<MobSpawnSettings.SpawnerData> monsters = biome.getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
        return new BiomeInfo(holder.unwrapKey().map(key -> key.location().toString()).orElse(""),
                new BiomeInfo.Colours(biome.getGrassColor(0, 0), biome.getFoliageColor(), biome.getWaterColor(),
                        biome.getSkyColor()),
                new BiomeInfo.Climate(biome.getBaseTemperature(), biome.hasPrecipitation()),
                new BiomeInfo.Spawns(!monsters.isEmpty(),
                        monsters.stream().anyMatch(spawn -> spawn.type == EntityType.SLIME),
                        holder.is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS)));
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
