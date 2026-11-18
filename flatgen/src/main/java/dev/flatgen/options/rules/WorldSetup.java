package dev.flatgen.options.rules;

import java.util.List;

/** What effects may change on the Create World screen when Done is pressed. */
public interface WorldSetup {
    /** Sets a game rule by name, its value as the game writes it ({@code false}, {@code 3}). */
    void setGameRule(String rule, String value);

    /** Adds structure sets, by id, to the world's chosen list; a world taking what its biome allows already has all. */
    void addStructureSets(List<String> sets);
}
