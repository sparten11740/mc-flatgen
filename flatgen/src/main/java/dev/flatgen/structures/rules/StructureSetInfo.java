package dev.flatgen.structures.rules;

import dev.flatgen.preset.rules.Ids;
import java.util.Set;

/**
 * What Flatgen shows of a structure set: its id and the biomes its structures can appear in.
 *
 * @param id     the set's id, such as {@code minecraft:villages}
 * @param biomes the biomes any of its structures can appear in, by id
 */
public record StructureSetInfo(String id, Set<String> biomes) {
    public StructureSetInfo {
        biomes = Set.copyOf(biomes);
    }

    /** Whether any of its structures can appear in this biome. */
    public boolean appearsIn(String biome) {
        return biomes.contains(biome);
    }

    /** A name for players made from its id, as structure sets have none of their own: "Ancient Cities". */
    public String name() {
        String path = id.substring(id.indexOf(':') + 1);
        StringBuilder name = new StringBuilder();
        for (String word : path.replace('/', ' ').split("[_ ]+")) {
            if (!word.isEmpty()) {
                name.append(name.length() == 0 ? "" : " ").append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
        }
        return name.toString();
    }

    /** The namespace of its id: the mod or data pack it comes from. */
    public String namespace() {
        return Ids.namespace(id);
    }
}
