# F05 Block picker

Milestone: M1 layer editor · Release: v1 · Status: done · Depends on: F04

## Why

Picking a block is the most frequent action after changing a height. In a pack with a hundred mods there are thousands
of blocks, so finding one has to be quick, and the picture has to be enough for players who cannot read.

## What the player gets

A block picker opened from the button showing the selected layer's block: a grid of block pictures to search by typing,
narrow to one mod, with the starred and recently picked blocks first. Clicking one sets the selected layer's block
and goes back. It is a screen of its own rather than a grid beside the column, which has no room for one at the
largest GUI scale.

## Acceptance criteria

- [x] Every block in the game's block registry, modded ones included, with its item picture; blocks without an item
  (water, lava, fire and the like) with their side, as the column draws it.
- [x] Search by name and by id, as the player types.
- [x] Filter by mod (namespace), with the mod's name from the loader (`ClientPlatform`).
- [x] Starred blocks first, then the last 16 picked, each under a heading; a right click stars or unstars; both kept
  across games in `config/flatgen/blocks.json`.
- [x] Hover shows name, id and mod.
- [x] Hints for blocks that make a poor layer, under the selected layer in the editor: a falling block over air, a
  fluid or the void, a block that is not whole (torches, flowers, water), a block entity. Their pictures come with
  F16.
- [x] Blocks disabled by the world's feature flags (experimental content not turned on) are hidden.

## Design

- `search/rules/Search` (pure, shared with the biome picker of F07 from the start): matching and ranking (exact id,
  name prefix, word prefix, contains), recent and starred ordering, over plain entries of id, name and namespace.
  Unit-tested once for both pickers.
- `blocks/client/BlockPicker`: the block picker, a `client/gui/PickerScreen` (the search field, the mod filter and
  Cancel around a `CardGrid`, shared with F07's biome picker) whose cards are block icons.
- `blocks/rules/SetBlock`: the edit, one step to undo; `blocks/rules/LayerHints`: the hints from a block's traits,
  which `mc/BlockCatalog` reads from the game; the starred and recent blocks are `search/rules/Picks`.
- The toolkit gains `TextField`, block icons on the `Canvas` and buttons whose label changes; config files share one
  reader, `config/ConfigFiles`.
- The list comes from the block registry of the world being created, filtered by its enabled feature flags, as the
  vanilla presets screen does (`filterFeatures`).
- Recent and starred blocks live in `config/flatgen/blocks.json`, the blocks feature's own file.

## Tests

- Unit: `SearchTest`, `PicksTest`, `PickSectionsTest`, `BlockRulesTest` (the edit and the hints), `TypedTextTest`,
  `PickerRowsTest`.
- GameTests on both nodes: the picker lists the game's blocks but air; sand, stone, water, a torch and a chest have
  the traits the hints read.
- Played with Blockpilot (`flatgen:blocks`, now `flatgen:picker`, lists the grid's cards): on NeoForge, search
  "glass" put glass first and picking it made the grass layer glass; it came back as recent, a right click starred
  stone, both kept in `blocks.json`; the mod filter showed the loader's name for the mod; sand picked for the bottom
  layer showed the falling hint. On Fabric, andesite picked through a search set the dirt layer.
- Not tried: a modded block (the dev runs have no block mods); blocks without an item drawn in the grid.
