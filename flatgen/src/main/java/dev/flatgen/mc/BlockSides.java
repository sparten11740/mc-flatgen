package dev.flatgen.mc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
//? if >=26.1 {
/*import net.minecraft.client.renderer.RenderPipelines;
*///?}
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks as they look from the side, for the editor's column: the sprites of their north face, each tinted as the game
 * tints it, drawn tiled. A block without faces of its own (water, lava) is drawn with its particle sprite. Looked up
 * once per block and kept, for one screen; textures change when resource packs do, so each screen starts afresh.
 */
public final class BlockSides {
    /** The size a block is drawn at, in GUI pixels, as in the inventory. */
    public static final int TILE = 16;
    /** White, the tint of a face the game does not tint. */
    static final int UNTINTED = 0xFFFFFF;
    /** The alpha a tint is drawn with, opaque (26.1). */
    private static final int OPAQUE = 0xFF000000;

    private final Map<String, List<Face>> faces = new HashMap<>();
    private final Map<String, ItemStack> items = new HashMap<>();

    /**
     * A sprite with the tint it is drawn in.
     *
     * @param sprite the sprite
     * @param tint   its tint, RGB, or {@link #FLUID} for the fluid colour given when drawing
     */
    record Face(TextureAtlasSprite sprite, int tint) {
        /** The tint that stands for the fluid colour the caller gives. */
        static final int FLUID = -1;
    }

    /** Fills the rectangle with the block's side, tiled from its top left corner; the caller clips what spills over. */
    public void draw(GuiGraphics graphics, String block, int fluidColor, int x, int y, int width, int height) {
        List<Face> sides = faces.computeIfAbsent(block, BlockSides::lookUp);
        for (int tileY = y; tileY < y + height; tileY += TILE) {
            for (int tileX = x; tileX < x + width; tileX += TILE) {
                drawTile(graphics, sides, fluidColor, tileX, tileY);
            }
        }
    }

    /** Draws the block's item at this corner, or its side when it has no item, as water and fire do not. */
    public void drawIcon(GuiGraphics graphics, String block, int x, int y) {
        ItemStack item = items.computeIfAbsent(block, BlockSides::itemOf);
        if (item.isEmpty()) {
            draw(graphics, block, Biomes.DEFAULT_WATER, x, y, TILE, TILE);
        } else {
            graphics.renderItem(item, x, y);
        }
    }

    private static ItemStack itemOf(String block) {
        ResourceLocation location = ResourceLocation.tryParse(block);
        return location == null ? ItemStack.EMPTY
                : BuiltInRegistries.BLOCK.getOptional(location).map(found -> new ItemStack(found.asItem()))
                        .orElse(ItemStack.EMPTY);
    }

    private static void drawTile(GuiGraphics graphics, List<Face> sides, int fluidColor, int x, int y) {
        for (Face face : sides) {
            int tint = face.tint() == Face.FLUID ? fluidColor : face.tint();
            //? if >=26.1 {
            /*graphics.blitSprite(RenderPipelines.GUI_TEXTURED, face.sprite(), x, y, TILE, TILE, OPAQUE | tint);
            *///?} else {
            graphics.blit(x, y, 0, TILE, TILE, face.sprite(),
                    (tint >> 16 & 0xFF) / 255F, (tint >> 8 & 0xFF) / 255F, (tint & 0xFF) / 255F, 1F);
            //?}
        }
    }

    /**
     * The faces a block shows from the side; none for air, as a layer of nothing (the game's void preset) is empty,
     * and the game's missing texture for a block it lacks.
     */
    private static List<Face> lookUp(String block) {
        ResourceLocation location = ResourceLocation.tryParse(block);
        Optional<Block> found = location == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(location);
        if (found.filter(known -> known.defaultBlockState().isAir()).isPresent()) {
            return List.of();
        }
        BlockState state = found.orElse(Blocks.AIR).defaultBlockState();
        List<Face> sides = BlockModels.northFaces(state);
        if (!sides.isEmpty()) {
            return sides;
        }
        return List.of(new Face(BlockModels.particle(state), state.getFluidState().isEmpty() ? UNTINTED : Face.FLUID));
    }
}
