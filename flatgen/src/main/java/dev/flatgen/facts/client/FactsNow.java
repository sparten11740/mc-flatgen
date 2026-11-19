package dev.flatgen.facts.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.rules.Situation;
import dev.flatgen.mc.Biomes;
import dev.flatgen.options.rules.GameRule;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The world a preset makes now, the editor's own unless told another, as the facts need it, worked out again only when
 * the biome, the surface or the options change; none for a biome the world being created lacks. Mob spawning is off
 * when an option that is on says so.
 */
public final class FactsNow {
    private final EditorContext editor;
    /** The preset whose world it is, as it is now. */
    private final Supplier<Preset> preset;
    private List<Object> situationFor;
    private Optional<Situation> situation;
    /** The options {@link #mobSpawning} was worked out for: a preset's, the same object until the next edit. */
    private Map<String, Boolean> spawningFor;
    private boolean mobSpawning;

    /** The world the editor shows, followed as it changes. */
    public FactsNow(EditorContext editor) {
        this(editor, () -> editor.shown().preset());
    }

    /** The world a preset makes in the editor's world, followed as the preset changes, as the templates show it. */
    public FactsNow(EditorContext editor, Supplier<Preset> preset) {
        this.editor = editor;
        this.preset = preset;
    }

    /** The world as the editor shows it now, but of another biome, as the biome picker's cards show it. */
    public Situation withBiome(BiomeInfo biome) {
        return Situation.of(biome, editor.world().floor() + preset.get().height(), mobSpawning());
    }

    /** The world as it is now. */
    public Optional<Situation> get() {
        String biome = preset.get().biome();
        int surface = editor.world().floor() + preset.get().height();
        boolean spawning = mobSpawning();
        List<Object> key = List.of(biome, surface, spawning);
        if (!key.equals(situationFor)) {
            situationFor = key;
            situation = Biomes.info(biome, editor.registries()).map(info -> Situation.of(info, surface, spawning));
        }
        return situation;
    }

    /** Whether mobs spawn: unless an option that is on turns the game rule off. */
    private boolean mobSpawning() {
        Map<String, Boolean> options = preset.get().options();
        if (options != spawningFor) {
            spawningFor = options;
            mobSpawning = !"false".equals(OptionChoices.applied(OptionChoices.on(options, editor.worldOptions()))
                    .gameRules().get(GameRule.MOB_SPAWNING));
        }
        return mobSpawning;
    }
}
