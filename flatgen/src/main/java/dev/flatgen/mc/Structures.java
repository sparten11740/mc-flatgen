package dev.flatgen.mc;

import dev.flatgen.structures.rules.StructureSetInfo;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/** The structure sets of the world being created, as this version lists them. */
public final class Structures {
    private Structures() {
    }

    /** Every structure set of these registries, the game's, mods' and data packs', in id order. */
    public static List<StructureSetInfo> all(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.STRUCTURE_SET).listElements()
                .map(set -> new StructureSetInfo(set.key().location().toString(), biomes(set.value())))
                .sorted(Comparator.comparing(StructureSetInfo::id)).toList();
    }

    /** The ids of every structure set of these registries. */
    public static Set<String> ids(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.STRUCTURE_SET).listElementIds()
                .map(key -> key.location().toString()).collect(Collectors.toSet());
    }

    /** The biomes any structure of a set can appear in, by id. */
    private static Set<String> biomes(StructureSet set) {
        return set.structures().stream()
                .flatMap(entry -> entry.structure().value().biomes().stream())
                .flatMap(biome -> biome.unwrapKey().stream())
                .map(key -> key.location().toString())
                .collect(Collectors.toSet());
    }
}
