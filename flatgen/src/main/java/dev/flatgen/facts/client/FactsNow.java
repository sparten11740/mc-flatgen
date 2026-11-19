package dev.flatgen.facts.client;

import dev.flatgen.biomes.rules.BiomeInfo;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.facts.rules.Situation;
import dev.flatgen.mc.Biomes;
import dev.flatgen.options.rules.GameRule;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.preset.rules.Stack;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The world a stack makes now, the editor's edited one unless told another, as the facts need it, worked out again only
 * when the biome, the surface or the options change; none for a biome the world being created lacks. Mob spawning is
 * off when an option that is on says so.
 */
public final class FactsNow {
    private final EditorContext editor;
    /** The stack whose world it is, as it is now. */
    private final Supplier<Stack> stack;
    /** The world options set otherwise than by default, as they are now. */
    private final Supplier<Map<String, Boolean>> options;
    private List<Object> situationFor;
    private Optional<Situation> situation;
    /** The options {@link #mobSpawning} was worked out for: a preset's, the same object until the next edit. */
    private Map<String, Boolean> spawningFor;
    private boolean mobSpawning;

    /** The world of the stack the editor shows, followed as it changes. */
    public FactsNow(EditorContext editor) {
        this(editor, () -> new Stack(editor.shown().layers(), editor.shown().biome()),
                () -> editor.shown().preset().options());
    }

    /** The world a preset's overworld makes in the editor's world, followed as the preset changes, for templates. */
    public FactsNow(EditorContext editor, Supplier<Preset> preset) {
        this(editor, () -> preset.get().stack(Dimension.OVERWORLD).orElseThrow(), () -> preset.get().options());
    }

    private FactsNow(EditorContext editor, Supplier<Stack> stack, Supplier<Map<String, Boolean>> options) {
        this.editor = editor;
        this.stack = stack;
        this.options = options;
    }

    /** The world as the editor shows it now, but of another biome, as the biome picker's cards show it. */
    public Situation withBiome(BiomeInfo biome) {
        return Situation.of(biome, editor.world().floor() + stack.get().height(), mobSpawning());
    }

    /** The world as it is now. */
    public Optional<Situation> get() {
        Stack now = stack.get();
        String biome = now.biome();
        int surface = editor.world().floor() + now.height();
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
        Map<String, Boolean> set = options.get();
        if (set != spawningFor) {
            spawningFor = set;
            mobSpawning = !"false".equals(OptionChoices.applied(OptionChoices.on(set, editor.worldOptions()))
                    .gameRules().get(GameRule.MOB_SPAWNING));
        }
        return mobSpawning;
    }
}
