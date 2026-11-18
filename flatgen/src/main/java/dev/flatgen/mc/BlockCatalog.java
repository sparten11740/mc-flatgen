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

    /** What a block is like as a layer; an unknown block is like air. */
    public static LayerHints.Traits traits(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        Block block = location == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(location).orElse(Blocks.AIR);
        BlockState state = block.defaultBlockState();
        boolean whole = state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        return new LayerHints.Traits(block instanceof FallingBlock, whole, state.hasBlockEntity(),
                whole && state.getFluidState().isEmpty());
    }
}
