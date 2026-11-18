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
     * Where a part of a code is and how it is written.
     *
     * @param start where it starts, as a character of the code counted from 0
     * @param end   where it ends, just after its last character
     * @param text  the part itself, as the code has it
     */
    public record Part(int start, int end, String text) {
        /** This part, as wrong in this way. */
        Problem as(Kind kind) {
            return new Problem(kind, start, end, text);
        }

        /** This part of a piece, placed in the whole code, where the piece starts at this character. */
        Part shifted(int pieceStart) {
            return new Part(pieceStart + start, pieceStart + end, text);
        }
    }

    /**
     * A code read: its layers and biome as far as they could be read, where it names them, and each part that is wrong.
     *
     * @param layers    the layers, bottom up
     * @param blocks    where the code names each layer's block, in the same order
     * @param biome     the biome's id
     * @param biomePart where the code names the biome; empty, at the end, when it leaves the biome out
     * @param problems  each part that is wrong; none for a code the game reads as it is
     */
    public record Reading(List<Layer> layers, List<Part> blocks, String biome, Part biomePart,
            List<Problem> problems) {
        public Reading {
            layers = List.copyOf(layers);
            blocks = List.copyOf(blocks);
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
        Part biomePart = partEnd < 0 ? new Part(code.length(), code.length(), "") : biomePart(code, partEnd + 1);
        String biome = biomePart.text().isEmpty() ? Preset.DEFAULT_BIOME : withNamespace(biomePart.text());
        List<Layer> layers = new ArrayList<>();
        List<Part> blocks = new ArrayList<>();
        List<Problem> problems = new ArrayList<>();
        int start = 0;
        for (String piece : layersPart.split(LAYER)) {
            Optional<Layer> layer = readLayer(piece.trim());
            if (layer.isPresent()) {
                layers.add(layer.get());
                blocks.add(blockPart(piece, start));
            } else if (!layersPart.isBlank()) {
                problems.add(new Problem(Kind.UNREADABLE_LAYER, start, start + piece.length(), piece.trim()));
            }
            start += piece.length() + LAYER.length();
        }
        if (layers.isEmpty() && problems.isEmpty()) {
            problems.add(new Problem(Kind.NO_LAYERS, 0, layersPart.length(), ""));
        }
        return new Reading(layers, blocks, biome, biomePart, problems);
    }

    /** Each block and the biome of a code read that the game lacks, as found by the editor, where the code names it. */
    public static List<Problem> unknownIds(Reading reading, MissingIds missing) {
        List<Problem> problems = new ArrayList<>();
        List<String> blocks = reading.layers().stream().map(Layer::block).toList();
        missing.blocks().forEach(block -> problems.add(reading.blocks().get(blocks.indexOf(block))
                .as(Kind.UNKNOWN_BLOCK)));
        if (missing.biomes().contains(reading.biome())) {
            problems.add(reading.biomePart().as(Kind.UNKNOWN_BIOME));
        }
        return problems;
    }

    /** Where the biome is named, from this character up to any further part, without the spaces around it. */
    private static Part biomePart(String code, int from) {
        int next = code.indexOf(PART, from);
        return trimmed(code, from, next < 0 ? code.length() : next);
    }

    /** Where a layer's block is named in its piece of the code, which starts at this character. */
    private static Part blockPart(String piece, int pieceStart) {
        int blockStart = piece.indexOf(HEIGHT) + 1;
        return trimmed(piece, blockStart, piece.length()).shifted(pieceStart);
    }

    /** The part of a text between two characters, without the spaces around it. */
    private static Part trimmed(String text, int from, int to) {
        String part = text.substring(from, to);
        int start = from + part.length() - part.stripLeading().length();
        return new Part(start, start + part.strip().length(), part.strip());
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
