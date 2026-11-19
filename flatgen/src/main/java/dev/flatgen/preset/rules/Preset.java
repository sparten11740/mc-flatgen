package dev.flatgen.preset.rules;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A superflat world as Flatgen keeps it: what the editor changes, the library saves and templates make. Ids are plain
 * text, so a preset naming a block, biome, structure set or option of a mod that is not installed still loads.
 *
 * @param name          what the player calls it
 * @param icon          the block shown for it, an id
 * @param layers        the layers from the bottom up
 * @param biome         the biome, an id
 * @param structureSets the structure sets that generate, ids or one tag ({@code #namespace:path}); empty for every set
 *                      the biome allows, the game's own default
 * @param decorations   whether the biome's features (trees, flowers, ores) generate
 * @param lakes         whether lava lakes generate
 * @param options       the world options (F27) set otherwise than by default, by id
 * @param stacks        the Nether's and the End's own superflat stacks (F22), by dimension; a dimension without one
 *                      is as the game makes it, as the overworld's is {@code layers} and {@code biome}
 */
public record Preset(String name, String icon, List<Layer> layers, String biome, Optional<List<String>> structureSets,
        boolean decorations, boolean lakes, Map<String, Boolean> options, Map<Dimension, Stack> stacks) {
    /** The biome of a preset that names none, as the game's own default. */
    public static final String DEFAULT_BIOME = "minecraft:plains";

    public Preset {
        layers = List.copyOf(layers);
        structureSets = structureSets.map(List::copyOf);
        options = Collections.unmodifiableMap(new TreeMap<>(options));
        EnumMap<Dimension, Stack> others = new EnumMap<>(Dimension.class);
        others.putAll(stacks);
        others.remove(Dimension.OVERWORLD);
        stacks = Collections.unmodifiableMap(others);
    }

    /** A preset of the overworld alone, the Nether and the End as the game makes them. */
    public Preset(String name, String icon, List<Layer> layers, String biome, Optional<List<String>> structureSets,
            boolean decorations, boolean lakes, Map<String, Boolean> options) {
        this(name, icon, layers, biome, structureSets, decorations, lakes, options, Map.of());
    }

    /** This preset under another name. */
    public Preset withName(String newName) {
        return new Preset(newName, icon, layers, biome, structureSets, decorations, lakes, options, stacks);
    }

    /** This preset with another icon, a block's id. */
    public Preset withIcon(String newIcon) {
        return new Preset(name, newIcon, layers, biome, structureSets, decorations, lakes, options, stacks);
    }

    /** This preset with its top layer's block as its icon, as the library shows it; as it is with no layers. */
    public Preset withTopBlockIcon() {
        return layers.isEmpty() ? this : withIcon(layers.get(layers.size() - 1).block());
    }

    /** This preset with other layers, bottom first. */
    public Preset withLayers(List<Layer> newLayers) {
        return new Preset(name, icon, newLayers, biome, structureSets, decorations, lakes, options, stacks);
    }

    /** This preset with another biome. */
    public Preset withBiome(String newBiome) {
        return new Preset(name, icon, layers, newBiome, structureSets, decorations, lakes, options, stacks);
    }

    /** This preset with other world options set otherwise than their default, by id. */
    public Preset withOptions(Map<String, Boolean> newOptions) {
        return new Preset(name, icon, layers, biome, structureSets, decorations, lakes, newOptions, stacks);
    }

    /** This preset with another chosen list of structure sets, or none to take what the biome allows. */
    public Preset withStructureSets(Optional<List<String>> newStructureSets) {
        return new Preset(name, icon, layers, biome, newStructureSets, decorations, lakes, options, stacks);
    }

    /** This preset with its decorations and lakes on or off. */
    public Preset withDecorations(boolean newDecorations, boolean newLakes) {
        return new Preset(name, icon, layers, biome, structureSets, newDecorations, newLakes, options, stacks);
    }

    /** A dimension's stack: the overworld's always, another's when the preset gives it one. */
    public Optional<Stack> stack(Dimension dimension) {
        return dimension == Dimension.OVERWORLD ? Optional.of(new Stack(layers, biome))
                : Optional.ofNullable(stacks.get(dimension));
    }

    /**
     * This preset with a dimension's stack set: the overworld's layers and biome, or another dimension's own stack,
     * none leaving that dimension as the game makes it.
     */
    public Preset withStack(Dimension dimension, Optional<Stack> stack) {
        if (dimension == Dimension.OVERWORLD) {
            Stack overworld = stack.orElseThrow(() -> new IllegalArgumentException("the overworld always has a stack"));
            return withLayers(overworld.layers()).withBiome(overworld.biome());
        }
        EnumMap<Dimension, Stack> changed = new EnumMap<>(Dimension.class);
        changed.putAll(stacks);
        stack.ifPresentOrElse(own -> changed.put(dimension, own), () -> changed.remove(dimension));
        return new Preset(name, icon, layers, biome, structureSets, decorations, lakes, options, changed);
    }

    /** How tall the stack is, in blocks. */
    public int height() {
        return layers.stream().mapToInt(Layer::height).sum();
    }

    /** Whether the stack fits a world this many blocks tall; layers above the top would be cut off. */
    public boolean fitsWorld(int worldHeight) {
        return height() <= worldHeight;
    }
}
