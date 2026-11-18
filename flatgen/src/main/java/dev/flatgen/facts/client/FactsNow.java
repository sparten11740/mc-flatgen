package dev.flatgen.facts.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.rules.WorldFacts;
import dev.flatgen.mc.Biomes;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.options.rules.WorldOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The world facts of what the editor shows now, worked out again only when the biome, the surface or the options
 * change; none for a biome the world being created lacks. Mob spawning is off when an option that is on says so.
 */
final class FactsNow {
    /** The game rule that turns mob spawning off, as 1.21.1 names it. */
    private static final String MOB_SPAWNING = "doMobSpawning";

    private final EditorContext editor;
    private final List<WorldOption> installed;
    private List<Object> factsFor;
    private Optional<WorldFacts> facts;
    private String biomeFor;
    private Optional<BiomeInfo> biome;
    /** The options {@link #mobSpawning} was worked out for: a preset's, the same object until the next edit. */
    private Map<String, Boolean> spawningFor;
    private boolean mobSpawning;

    FactsNow(EditorContext editor) {
        this.editor = editor;
        this.installed = editor.worldOptions();
    }

    /** The facts of the world as the editor shows it now. */
    Optional<WorldFacts> get() {
        int surface = editor.world().floor() + editor.shown().preset().height();
        boolean spawning = mobSpawning();
        List<Object> key = List.of(editor.shown().preset().biome(), surface, spawning);
        if (!key.equals(factsFor)) {
            factsFor = key;
            facts = biome().map(info -> WorldFacts.of(info, surface, spawning));
        }
        return facts;
    }

    /** What Flatgen knows of the biome the editor shows now. */
    Optional<BiomeInfo> biome() {
        String id = editor.shown().preset().biome();
        if (!id.equals(biomeFor)) {
            biomeFor = id;
            biome = Biomes.info(id, editor.registries());
        }
        return biome;
    }

    /** Whether mobs spawn: unless an option that is on turns the game rule off. */
    boolean mobSpawning() {
        Map<String, Boolean> options = editor.shown().preset().options();
        if (options != spawningFor) {
            spawningFor = options;
            mobSpawning = !"false".equals(OptionChoices.applied(OptionChoices.on(options, installed)).gameRules()
                    .get(MOB_SPAWNING));
        }
        return mobSpawning;
    }
}
