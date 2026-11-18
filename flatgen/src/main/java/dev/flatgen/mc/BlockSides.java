package dev.flatgen.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
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
    private static final int UNTINTED = 0xFFFFFF;
    /** What the game's block colours answer for a block with no colour of its own, without a world to ask. */
    private static final int NO_COLOR = -1;
    /** The seed the faces are picked with, so a block with random variants always looks the same. */
    private static final long SEED = 42L;

    private final Map<String, List<Face>> faces = new HashMap<>();
    private final Map<String, ItemStack> items = new HashMap<>();

    /** A sprite with the tint it is drawn in, RGB, or {@link #FLUID} for the fluid colour given when drawing. */
    private record Face(TextureAtlasSprite sprite, int tint) {
        /** The tint that stands for the fluid colour the caller gives. */
        static final int FLUID = -1;
    }

    /** Fills the rectangle with the block's side, tiled, clipped to the rectangle. */
    public void draw(GuiGraphics graphics, String block, int fluidColor, int x, int y, int width, int height) {
        List<Face> sides = faces.computeIfAbsent(block, BlockSides::lookUp);
        graphics.enableScissor(x, y, x + width, y + height);
        for (int tileY = y; tileY < y + height; tileY += TILE) {
            for (int tileX = x; tileX < x + width; tileX += TILE) {
                drawTile(graphics, sides, fluidColor, tileX, tileY);
            }
        }
        graphics.disableScissor();
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
            graphics.blit(x, y, 0, TILE, TILE, face.sprite(),
                    (tint >> 16 & 0xFF) / 255F, (tint >> 8 & 0xFF) / 255F, (tint & 0xFF) / 255F, 1F);
        }
    }

    private static List<Face> lookUp(String block) {
        ResourceLocation location = ResourceLocation.tryParse(block);
        BlockState state = (location == null ? Blocks.AIR
                : BuiltInRegistries.BLOCK.getOptional(location).orElse(Blocks.AIR)).defaultBlockState();
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
        List<Face> sides = new ArrayList<>();
        for (BakedQuad quad : model.getQuads(state, Direction.NORTH, RandomSource.create(SEED))) {
            int tint = quad.isTinted()
                    ? minecraft.getBlockColors().getColor(state, null, null, quad.getTintIndex()) : UNTINTED;
            sides.add(new Face(quad.getSprite(), tint == NO_COLOR ? UNTINTED : tint));
        }
        if (sides.isEmpty()) {
            sides.add(new Face(model.getParticleIcon(), state.getFluidState().isEmpty() ? UNTINTED : Face.FLUID));
        }
        return sides;
    }
}
