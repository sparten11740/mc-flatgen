package dev.flatgen.facts.client;

import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.rules.Situation;
import dev.flatgen.mc.Biomes;
import dev.flatgen.options.rules.GameRule;
import dev.flatgen.options.rules.OptionChoices;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The world the editor shows now, as the facts need it, worked out again only when the biome, the surface or the
 * options change; none for a biome the world being created lacks. Mob spawning is off when an option that is on says
 * so.
 */
final class FactsNow {
    private final EditorContext editor;
    private List<Object> situationFor;
    private Optional<Situation> situation;
    /** The options {@link #mobSpawning} was worked out for: a preset's, the same object until the next edit. */
    private Map<String, Boolean> spawningFor;
    private boolean mobSpawning;

    FactsNow(EditorContext editor) {
        this.editor = editor;
    }

    /** The world as the editor shows it now. */
    Optional<Situation> get() {
        String biome = editor.shown().preset().biome();
        int surface = editor.world().floor() + editor.shown().preset().height();
        boolean spawning = mobSpawning();
        List<Object> key = List.of(biome, surface, spawning);
        if (!key.equals(situationFor)) {
            situationFor = key;
            situation = Biomes.info(biome, editor.registries()).map(info -> Situation.of(info, surface, spawning));
        }
        return situation;
    }

    /** Whether mobs spawn: unless an option that is on turns the game rule off. */
    boolean mobSpawning() {
        Map<String, Boolean> options = editor.shown().preset().options();
        if (options != spawningFor) {
            spawningFor = options;
            mobSpawning = !"false".equals(OptionChoices.applied(OptionChoices.on(options, editor.worldOptions()))
                    .gameRules().get(GameRule.MOB_SPAWNING));
        }
        return mobSpawning;
    }
}
