package dev.flatgen.layers.rules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

/**
 * Where each layer of a stack is drawn in the editor's column, as pixel spans from the column's top. Layers keep their
 * proportions where they can, but none is thinner than a minimum, so a 1-block layer beside a 100-block one stays
 * visible and clickable; when even the minimums do not fit, the column grows taller than its space and scrolls.
 *
 * @param spans         each layer's span, bottom layer first, as the layers are
 * @param contentHeight the column's whole height in pixels, the space given or more when it scrolls
 */
public record ColumnLayout(List<Span> spans, int contentHeight) {
    /**
     * A layer's pixels: from {@code top} (inclusive) down to {@code bottom} (exclusive), from the column's top.
     *
     * @param top    the layer's top edge
     * @param bottom the layer's bottom edge, below the top
     */
    public record Span(int top, int bottom) {
        /** How many pixels tall it is. */
        public int height() {
            return bottom - top;
        }
    }

    public ColumnLayout {
        spans = List.copyOf(spans);
    }

    /**
     * The layout of layers this many blocks deep each, bottom first, in this many pixels, none thinner than the
     * minimum.
     */
    public static ColumnLayout of(List<Integer> heights, int space, int minimum) {
        int contentHeight = Math.max(space, heights.size() * minimum);
        int[] pixels = pixels(heights, contentHeight, minimum);
        List<Span> spans = new ArrayList<>();
        int bottom = contentHeight;
        for (int pixel : pixels) {
            spans.add(new Span(bottom - pixel, bottom));
            bottom -= pixel;
        }
        return new ColumnLayout(spans, contentHeight);
    }

    /**
     * How tall a stack is drawn in a column: its share of the world's height, so the air above it shows as the world
     * has it, but no less than the least it is given and no more than the column less the headroom kept above it.
     *
     * @param column      the column's height, in pixels
     * @param headroom    the pixels kept free above the stack when it would fill the column
     * @param blocks      how many blocks the stack has, all layers together
     * @param worldHeight how many blocks tall the world is
     * @param least       the least pixels the stack takes
     */
    public static int stackSpace(int column, int headroom, int blocks, int worldHeight, Least least) {
        int share = (int) ((long) column * blocks / Math.max(1, worldHeight));
        return Math.min(column - headroom, Math.max(share, Math.max(least.forLayers(), least.forGround())));
    }

    /**
     * The least a stack takes of its column, in pixels: enough for each layer to show, and enough that the ground is a
     * good share of the picture however thin the stack.
     *
     * @param forLayers the least for its layers together
     * @param forGround the least share of the column
     */
    public record Least(int forLayers, int forGround) {
    }

    /** The layer drawn at this pixel from the column's top, if any. */
    public OptionalInt layerAt(int pixel) {
        for (int index = 0; index < spans.size(); index++) {
            Span span = spans.get(index);
            if (pixel >= span.top() && pixel < span.bottom()) {
                return OptionalInt.of(index);
            }
        }
        return OptionalInt.empty();
    }

    /**
     * The pixel row of the line between block Y-1 and block Y, for layers this many blocks deep starting at the world's
     * floor; within a layer the line sits in proportion to the blocks below it. Empty when Y is outside the stack.
     */
    public OptionalInt pixelOfLine(int y, int floor, List<Integer> heights) {
        int layerFloor = floor;
        for (int index = 0; index < heights.size(); index++) {
            int height = heights.get(index);
            if (y >= layerFloor && y <= layerFloor + height) {
                Span span = spans.get(index);
                return OptionalInt.of(span.bottom() - Math.round((float) (y - layerFloor) * span.height() / height));
            }
            layerFloor += height;
        }
        return OptionalInt.empty();
    }

    /**
     * Each layer's share of the pixels: in proportion to its height, but at least the minimum, the rest shared among
     * the others in proportion again, and rounded so they add up to the whole exactly.
     */
    private static int[] pixels(List<Integer> heights, int total, int minimum) {
        boolean[] atMinimum = new boolean[heights.size()];
        double[] shares = shares(heights, total, minimum, atMinimum);
        while (markTooThin(shares, minimum, atMinimum)) {
            shares = shares(heights, total, minimum, atMinimum);
        }
        return rounded(shares, total);
    }

    /** The layers' shares when those marked get the minimum and the others share the rest by height. */
    private static double[] shares(List<Integer> heights, int total, int minimum, boolean[] atMinimum) {
        double free = total;
        long freeBlocks = 0;
        for (int index = 0; index < atMinimum.length; index++) {
            free -= atMinimum[index] ? minimum : 0;
            freeBlocks += atMinimum[index] ? 0 : heights.get(index);
        }
        double[] shares = new double[atMinimum.length];
        for (int index = 0; index < atMinimum.length; index++) {
            shares[index] = atMinimum[index] ? minimum : heights.get(index) * free / freeBlocks;
        }
        return shares;
    }

    /** Marks the layers whose share fell below the minimum; whether any was. */
    private static boolean markTooThin(double[] shares, int minimum, boolean[] atMinimum) {
        boolean marked = false;
        for (int index = 0; index < shares.length; index++) {
            if (!atMinimum[index] && shares[index] < minimum) {
                atMinimum[index] = true;
                marked = true;
            }
        }
        return marked;
    }

    /** Whole pixels for each share, adding up to the total, the leftover pixels going to the largest remainders. */
    private static int[] rounded(double[] shares, int total) {
        int[] pixels = new int[shares.length];
        List<Integer> byRemainder = new ArrayList<>();
        int used = 0;
        for (int index = 0; index < shares.length; index++) {
            pixels[index] = (int) Math.floor(shares[index]);
            used += pixels[index];
            byRemainder.add(index);
        }
        byRemainder.sort(Comparator.comparingDouble((Integer index) -> shares[index] - pixels[index]).reversed());
        for (int index : byRemainder.subList(0, Math.min(byRemainder.size(), total - used))) {
            pixels[index]++;
        }
        return pixels;
    }
}
