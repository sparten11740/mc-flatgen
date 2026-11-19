package dev.flatgen.structures.client;

import dev.flatgen.client.gui.Icon;
import java.util.Map;

/** The picture of each of the game's structure sets, for players who read pictures first; a structure block else. */
final class StructureIcons {
    private static final Icon OTHER = Icon.item("minecraft:structure_block");
    private static final Map<String, String> ITEMS = Map.ofEntries(
            Map.entry("minecraft:ancient_cities", "minecraft:sculk_shrieker"),
            Map.entry("minecraft:buried_treasures", "minecraft:heart_of_the_sea"),
            Map.entry("minecraft:desert_pyramids", "minecraft:chiseled_sandstone"),
            Map.entry("minecraft:end_cities", "minecraft:purpur_block"),
            Map.entry("minecraft:igloos", "minecraft:snow_block"),
            Map.entry("minecraft:jungle_temples", "minecraft:mossy_cobblestone"),
            Map.entry("minecraft:mineshafts", "minecraft:minecart"),
            Map.entry("minecraft:nether_complexes", "minecraft:nether_bricks"),
            Map.entry("minecraft:nether_fossils", "minecraft:bone_block"),
            Map.entry("minecraft:ocean_monuments", "minecraft:prismarine"),
            Map.entry("minecraft:ocean_ruins", "minecraft:cracked_stone_bricks"),
            Map.entry("minecraft:pillager_outposts", "minecraft:crossbow"),
            Map.entry("minecraft:ruined_portals", "minecraft:crying_obsidian"),
            Map.entry("minecraft:shipwrecks", "minecraft:oak_boat"),
            Map.entry("minecraft:strongholds", "minecraft:ender_eye"),
            Map.entry("minecraft:swamp_huts", "minecraft:cauldron"),
            Map.entry("minecraft:trail_ruins", "minecraft:brush"),
            Map.entry("minecraft:trial_chambers", "minecraft:trial_key"),
            Map.entry("minecraft:villages", "minecraft:bell"),
            Map.entry("minecraft:woodland_mansions", "minecraft:totem_of_undying"));

    private StructureIcons() {
    }

    /** A structure set's picture. */
    static Icon of(String set) {
        String item = ITEMS.get(set);
        return item == null ? OTHER : Icon.item(item);
    }
}
