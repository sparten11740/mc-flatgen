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
//? if >=26.1 {
/*import net.minecraft.world.attribute.EnvironmentAttributes;
*///?}
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

/** What Flatgen shows of biomes, from the registries of the world being created. */
public final class Biomes {
    //? if >=1.21.2 {
    /*/^* The sea level a superflat world's generator keeps. ^/
    private static final int FLAT_SEA_LEVEL = -63;
    /^* How far above its sea level a world starts to cool with height, in blocks. ^/
    private static final int COOLING_ABOVE_SEA_LEVEL = 17;
    /^* Above this Y a superflat world cools with height. ^/
    private static final int COOLING_START_Y = FLAT_SEA_LEVEL + COOLING_ABOVE_SEA_LEVEL;
    /^* The overworld's own sky colour, RGB, which a biome's sky modifies; a biome that leaves it alone shows it. ^/
    private static final int OVERWORLD_SKY = 0x78A7FF;
    *///?} else {
    /** Above this Y the game cools a world with height, whatever its sea level. */
    private static final int COOLING_START_Y = 80;
    //?}
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
        //? if >=26.1 {
        /*List<EntityType<?>> monsters = biome.getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                .<EntityType<?>>map(spawn -> spawn.value().type()).toList();
        int sky = biome.getAttributes().applyModifier(EnvironmentAttributes.SKY_COLOR, OVERWORLD_SKY);
        *///?} else {
        List<EntityType<?>> monsters = biome.getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                .<EntityType<?>>map(spawn -> spawn.type).toList();
        int sky = biome.getSkyColor();
        //?}
        return new BiomeInfo(holder.unwrapKey().map(key -> key.location().toString()).orElse(""),
                new BiomeInfo.Colours(biome.getGrassColor(0, 0), biome.getFoliageColor(), biome.getWaterColor(), sky),
                new BiomeInfo.Climate(biome.getBaseTemperature(), biome.hasPrecipitation(), COOLING_START_Y),
                new BiomeInfo.Spawns(!monsters.isEmpty(), monsters.contains(EntityType.SLIME),
                        holder.is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS)));
    }

    /** A biome's water colour, RGB. */
    public static int waterColor(String biome, HolderLookup.Provider registries) {
        return info(biome, registries).map(found -> found.colours().water()).orElse(DEFAULT_WATER);
    }
}
