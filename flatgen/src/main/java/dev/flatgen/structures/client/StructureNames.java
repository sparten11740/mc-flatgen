package dev.flatgen.structures.client;

import dev.flatgen.structures.rules.StructureSetInfo;
import java.util.Map;
import net.minecraft.network.chat.Component;

/**
 * The structure sets' names for players: the game gives sets no names of its own, so the game's sets have Flatgen's,
 * in the game's words for them in each language, and a mod's set is named from its id.
 */
final class StructureNames {
    /** The translation of each of the game's structure sets' names, by the set's id. */
    private static final Map<String, String> KEYS = Map.ofEntries(
            Map.entry("minecraft:ancient_cities", "flatgen.structure_set.minecraft.ancient_cities"),
            Map.entry("minecraft:buried_treasures", "flatgen.structure_set.minecraft.buried_treasures"),
            Map.entry("minecraft:desert_pyramids", "flatgen.structure_set.minecraft.desert_pyramids"),
            Map.entry("minecraft:end_cities", "flatgen.structure_set.minecraft.end_cities"),
            Map.entry("minecraft:igloos", "flatgen.structure_set.minecraft.igloos"),
            Map.entry("minecraft:jungle_temples", "flatgen.structure_set.minecraft.jungle_temples"),
            Map.entry("minecraft:mineshafts", "flatgen.structure_set.minecraft.mineshafts"),
            Map.entry("minecraft:nether_complexes", "flatgen.structure_set.minecraft.nether_complexes"),
            Map.entry("minecraft:nether_fossils", "flatgen.structure_set.minecraft.nether_fossils"),
            Map.entry("minecraft:ocean_monuments", "flatgen.structure_set.minecraft.ocean_monuments"),
            Map.entry("minecraft:ocean_ruins", "flatgen.structure_set.minecraft.ocean_ruins"),
            Map.entry("minecraft:pillager_outposts", "flatgen.structure_set.minecraft.pillager_outposts"),
            Map.entry("minecraft:ruined_portals", "flatgen.structure_set.minecraft.ruined_portals"),
            Map.entry("minecraft:shipwrecks", "flatgen.structure_set.minecraft.shipwrecks"),
            Map.entry("minecraft:strongholds", "flatgen.structure_set.minecraft.strongholds"),
            Map.entry("minecraft:swamp_huts", "flatgen.structure_set.minecraft.swamp_huts"),
            Map.entry("minecraft:trail_ruins", "flatgen.structure_set.minecraft.trail_ruins"),
            Map.entry("minecraft:trial_chambers", "flatgen.structure_set.minecraft.trial_chambers"),
            Map.entry("minecraft:villages", "flatgen.structure_set.minecraft.villages"),
            Map.entry("minecraft:woodland_mansions", "flatgen.structure_set.minecraft.woodland_mansions"));

    private StructureNames() {
    }

    /** A structure set's name for players. */
    static Component of(StructureSetInfo set) {
        String key = KEYS.get(set.id());
        return key == null ? Component.literal(set.name()) : Component.translatable(key);
    }
}
