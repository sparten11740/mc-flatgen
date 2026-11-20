package dev.flatgen.mc;

import dev.flatgen.blocks.rules.LayerHints;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The blocks a layer can be of, from the game's block registry, what each is like as a layer, and the creative
 * inventory's tabs they are sorted into.
 */
public final class BlockCatalog {
    /** The creative inventory's tabs of blocks, in its order: building, coloured, natural, functional, redstone. */
    private static final List<String> BLOCK_TABS = List.of("minecraft:building_blocks", "minecraft:colored_blocks",
            "minecraft:natural_blocks", "minecraft:functional_blocks", "minecraft:redstone_blocks");

    /**
     * A tab of the creative inventory as a kind of block.
     *
     * @param name   its name, as the game shows it in the player's language
     * @param icon   its picture, an item's id, as the game shows it
     * @param blocks the ids of the blocks in it, in the order the creative inventory shows them
     */
    public record Tab(Component name, String icon, List<String> blocks) {
        public Tab {
            blocks = List.copyOf(blocks);
        }
    }

    private BlockCatalog() {
    }

    /**
     * The creative inventory's tabs of blocks, as the game fills them for these features, mods' blocks put in them
     * included; each tab the game lacks is left out.
     */
    public static List<Tab> tabs(FeatureFlagSet enabled, HolderLookup.Provider registries) {
        CreativeModeTabs.tryRebuildTabContents(enabled, false, registries);
        return BLOCK_TABS.stream().map(id -> ResourceLocation.tryParse(id)).filter(Objects::nonNull)
                .flatMap(id -> BuiltInRegistries.CREATIVE_MODE_TAB.getOptional(id).stream())
                .map(BlockCatalog::tab).toList();
    }

    private static Tab tab(CreativeModeTab tab) {
        List<String> blocks = tab.getDisplayItems().stream().map(ItemStack::getItem)
                .filter(BlockItem.class::isInstance)
                .map(item -> BuiltInRegistries.BLOCK.getKey(((BlockItem) item).getBlock()).toString())
                .distinct().toList();
        return new Tab(tab.getDisplayName(), BuiltInRegistries.ITEM.getKey(tab.getIconItem().getItem()).toString(),
                blocks);
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
     * What a block is like as a layer; an unknown block is like air. Suspicious sand and gravel fall as sand does,
     * though the game makes them brushable blocks rather than falling ones.
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
