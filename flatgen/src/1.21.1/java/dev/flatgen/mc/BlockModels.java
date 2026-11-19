package dev.flatgen.mc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** A block's model as this version bakes it, for {@link BlockSides}: its north faces and its particle sprite. */
final class BlockModels {
    /** What the game's block colours answer for a block with no colour of its own, without a world to ask. */
    private static final int NO_COLOR = -1;
    /** The seed the faces are picked with, so a block with random variants always looks the same. */
    private static final long SEED = 42L;

    private BlockModels() {
    }

    /** The faces a block shows from the north, each tinted as the game tints it without a world to ask. */
    static List<BlockSides.Face> northFaces(BlockState state) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
        List<BlockSides.Face> faces = new ArrayList<>();
        for (BakedQuad quad : model.getQuads(state, Direction.NORTH, RandomSource.create(SEED))) {
            int tint = quad.isTinted() ? minecraft.getBlockColors().getColor(state, null, null, quad.getTintIndex())
                    : BlockSides.UNTINTED;
            faces.add(new BlockSides.Face(quad.getSprite(), tint == NO_COLOR ? BlockSides.UNTINTED : tint));
        }
        return faces;
    }

    /** The sprite a block's particles show, which stands for a block without faces of its own. */
    static TextureAtlasSprite particle(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getParticleIcon();
    }
}
