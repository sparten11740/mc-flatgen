package dev.flatgen.mc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block's model as this version bakes it, for {@link BlockSides}: the faces its parts show from the north, tinted as
 * the game's block colours tint them without a world to ask, and its particle.
 */
final class BlockModels {
    /** What the block colours answer for a block with no colour of its own, without a world to ask. */
    private static final int NO_COLOR = -1;
    /** The seed the parts are picked with, so a block with random variants always looks the same. */
    private static final long SEED = 42L;

    private BlockModels() {
    }

    /** The faces a block shows from the north, each tinted as the game tints it without a world to ask. */
    static List<BlockSides.Face> northFaces(BlockState state) {
        List<BlockModelPart> parts = new ArrayList<>();
        model(state).collectParts(RandomSource.create(SEED), parts);
        List<BlockSides.Face> faces = new ArrayList<>();
        for (BlockModelPart part : parts) {
            part.getQuads(Direction.NORTH).forEach(quad -> faces.add(face(state, quad)));
        }
        return faces;
    }

    /** The sprite a block's particles show, which stands for a block without faces of its own. */
    static TextureAtlasSprite particle(BlockState state) {
        return model(state).particleIcon();
    }

    private static BlockStateModel model(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
    }

    private static BlockSides.Face face(BlockState state, BakedQuad quad) {
        int tint = quad.isTinted()
                ? Minecraft.getInstance().getBlockColors().getColor(state, null, null, quad.tintIndex())
                : BlockSides.UNTINTED;
        return new BlockSides.Face(quad.sprite(), tint == NO_COLOR ? BlockSides.UNTINTED
                : tint & BlockSides.UNTINTED);
    }
}
