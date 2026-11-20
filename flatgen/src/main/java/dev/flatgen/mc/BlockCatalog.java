package dev.flatgen.mc;

import dev.flatgen.blocks.rules.LayerHints;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The blocks a layer can be of, from the game's block registry, and what each is like as a layer. */
public final class BlockCatalog {
    private BlockCatalog() {
    }

    /** Every block the world's enabled features allow, but air, in the registry's order, as search entries. */
    public static List<Search.Entry> blocks(FeatureFlagSet enabled) {
        List<Search.Entry> entries = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block.isEnabled(enabled) && !block.defaultBlockState().isAir()) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                entries.add(new Search.Entry(id.toString(), block.getName().getString(), id.getNamespace()));
            }
        }
        return entries;
    }

    /**
     * What a block is like as a layer; an unknown block is like air. Suspicious sand and gravel fall as sand does, though
     * the game makes them brushable blocks rather than falling ones.
     */
    public static LayerHints.Traits traits(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        Block block = location == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(location).orElse(Blocks.AIR);
        BlockState state = block.defaultBlockState();
        boolean whole = state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        boolean falls = block instanceof FallingBlock || block instanceof BrushableBlock;
        return new LayerHints.Traits(falls, whole, state.hasBlockEntity(),
                !FallingBlock.isFree(state));
    }
}
