package dev.flatgen.preset.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The game's superflat code, the text its presets screen shares worlds as:
 * {@code minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;minecraft:plains}, the layers bottom up, each
 * {@code [height*]block}, then the biome, plains when it is left out. Flatgen writes it exactly as the game does, and
 * reads anything the game reads (ids without a namespace are the game's, a trailing comma is ignored), saying which
 * part is wrong where the game quietly falls back to its default world.
 */
public final class VanillaCode {
    private static final char PART = ';';
    private static final String LAYER = ",";
    private static final char HEIGHT = '*';
    /** What an id may be made of, as the game's ids. */
    private static final String ID = "[a-z0-9_.-]+(:[a-z0-9_./-]+)?";
    /** A layer's height as the code writes it: one to four digits. */
    private static final String DEPTH = "\\d{1,4}";
    /** The namespace of an id written without one, as the game reads it. */
    private static final String GAME_NAMESPACE = "minecraft:";

    private VanillaCode() {
    }

    /** What is wrong with a part of a code. */
    public enum Kind {
        /** A layer that is not {@code [height*]block}, or whose height is not at least 1. */
        UNREADABLE_LAYER,
        /** A code with no layers. */
        NO_LAYERS,
        /** A block the game lacks, found by the editor. */
        UNKNOWN_BLOCK,
        /** A biome the game lacks, found by the editor. */
        UNKNOWN_BIOME,
        /** Layers taller than the world, found by the editor. */
        TOO_TALL
    }

    /**
     * A part of a code that is wrong.
     *
     * @param kind  what is wrong with it
     * @param start where it starts, as a character of the code counted from 0
     * @param end   where it ends, just after its last character
     * @param text  the part itself, as the code has it
     */
    public record Problem(Kind kind, int start, int end, String text) {
    }

    /**
     * A code read: its layers and biome as far as they could be read, and each part that is wrong.
     *
     * @param layers   the layers, bottom up
     * @param biome    the biome's id
     * @param problems each part that is wrong; none for a code the game reads as it is
     */
    public record Reading(List<Layer> layers, String biome, List<Problem> problems) {
        public Reading {
            layers = List.copyOf(layers);
            problems = List.copyOf(problems);
        }
    }

    /** The code of a preset's layers and biome, exactly as the game's presets screen writes it. */
    public static String write(Preset preset) {
        return preset.layers().stream().map(VanillaCode::layer).collect(Collectors.joining(LAYER)) + PART
                + preset.biome();
    }

    /** Reads a code, noting each layer the game could not read, or that there are none. */
    public static Reading read(String code) {
        int partEnd = code.indexOf(PART);
        String layersPart = partEnd < 0 ? code : code.substring(0, partEnd);
        String biome = partEnd < 0 ? Preset.DEFAULT_BIOME : biomeOf(code.substring(partEnd + 1));
        if (layersPart.isBlank()) {
            return new Reading(List.of(), biome, List.of(new Problem(Kind.NO_LAYERS, 0, layersPart.length(), "")));
        }
        List<Layer> layers = new ArrayList<>();
        List<Problem> problems = new ArrayList<>();
        int start = 0;
        for (String piece : layersPart.split(LAYER)) {
            Optional<Layer> layer = readLayer(piece.trim());
            if (layer.isPresent()) {
                layers.add(layer.get());
            } else {
                problems.add(new Problem(Kind.UNREADABLE_LAYER, start, start + piece.length(), piece.trim()));
            }
            start += piece.length() + LAYER.length();
        }
        if (layers.isEmpty() && problems.isEmpty()) {
            problems.add(new Problem(Kind.NO_LAYERS, 0, layersPart.length(), ""));
        }
        return new Reading(layers, biome, problems);
    }

    /** The biome of the part after the layers, up to any further part; plains when it is empty. */
    private static String biomeOf(String rest) {
        int next = rest.indexOf(PART);
        String biome = (next < 0 ? rest : rest.substring(0, next)).trim();
        return biome.isEmpty() ? Preset.DEFAULT_BIOME : withNamespace(biome);
    }

    /** A layer from its piece of the code, {@code [height*]block}; none when the piece cannot be read. */
    private static Optional<Layer> readLayer(String piece) {
        int star = piece.indexOf(HEIGHT);
        String block = (star < 0 ? piece : piece.substring(star + 1)).trim();
        String depth = star < 0 ? "1" : piece.substring(0, star).trim();
        boolean readable = block.matches(ID) && depth.matches(DEPTH) && Integer.parseInt(depth) >= 1;
        return readable ? Optional.of(new Layer(withNamespace(block), Integer.parseInt(depth))) : Optional.empty();
    }

    /** An id with the game's namespace when it is written without one, as the game reads {@code stone}. */
    private static String withNamespace(String id) {
        return id.contains(":") ? id : GAME_NAMESPACE + id;
    }

    private static String layer(Layer layer) {
        return (layer.height() != 1 ? layer.height() + "*" : "") + layer.block();
    }
}
