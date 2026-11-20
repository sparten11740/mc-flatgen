package dev.flatgen.mc;

import dev.flatgen.Flatgen;
import dev.flatgen.blocks.rules.LayerHints;
import dev.flatgen.search.rules.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    /**
     * The creative inventory's tabs of blocks: natural first, as most of a flat world's layers are, then building,
     * coloured, functional and redstone, as the creative inventory has them.
     */
    private static final List<String> BLOCK_TABS = List.of("minecraft:natural_blocks", "minecraft:building_blocks",
            "minecraft:colored_blocks", "minecraft:functional_blocks", "minecraft:redstone_blocks");
    /** The game's own namespace, whose blocks the picker lists first. */
    private static final String GAME = "minecraft";

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
     * The creative inventory's tabs of blocks, each filled as the game fills it for these features, mods' blocks put
     * in it included. Only these tabs are filled, one by one: filling runs mods' code, which may expect a world or a
     * server's settings the Create World screen has not got, so a tab whose filling fails is left out, as is one the
     * game lacks, and the log says why.
     */
    public static List<Tab> tabs(FeatureFlagSet enabled, HolderLookup.Provider registries) {
        CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(enabled, false,
                registries);
        List<Tab> tabs = new ArrayList<>();
        for (String id : BLOCK_TABS) {
            filled(id, parameters).ifPresent(tabs::add);
        }
        return tabs;
    }

    private static Optional<Tab> filled(String id, CreativeModeTab.ItemDisplayParameters parameters) {
        Optional<CreativeModeTab> tab = Optional.ofNullable(ResourceLocation.tryParse(id))
                .flatMap(BuiltInRegistries.CREATIVE_MODE_TAB::getOptional);
        try {
            tab.ifPresent(found -> found.buildContents(parameters));
            return tab.map(BlockCatalog::tab);
        } catch (RuntimeException failure) {
            Flatgen.LOGGER.warn("Flatgen could not fill the creative tab {}, so the block picker goes without it", id,
                    failure);
            return Optional.empty();
        }
    }

    private static Tab tab(CreativeModeTab tab) {
        List<String> blocks = tab.getDisplayItems().stream().map(ItemStack::getItem)
                .filter(BlockItem.class::isInstance)
                .map(item -> BuiltInRegistries.BLOCK.getKey(((BlockItem) item).getBlock()).toString())
                .distinct().toList();
        return new Tab(tab.getDisplayName(), BuiltInRegistries.ITEM.getKey(tab.getIconItem().getItem()).toString(),
                blocks);
    }

    /**
     * Every block the world's enabled features allow as search entries, the game's own first and then the mods', each
     * in the registry's order; but air, and mods' blocks with no item, which are the parts mods move, light or mark
     * with rather than blocks to build with, and show no picture.
     */
    public static List<Search.Entry> blocks(FeatureFlagSet enabled) {
        List<Search.Entry> games = new ArrayList<>();
        List<Search.Entry> mods = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (listed(block, id, enabled)) {
                (id.getNamespace().equals(GAME) ? games : mods).add(new Search.Entry(id.toString(),
                        block.getName().getString(), id.getNamespace()));
            }
        }
        games.addAll(mods);
        return games;
    }

    private static boolean listed(Block block, ResourceLocation id, FeatureFlagSet enabled) {
        return block.isEnabled(enabled) && !block.defaultBlockState().isAir()
                && (block.asItem() != Items.AIR || id.getNamespace().equals(GAME));
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
