# F05 Block picker

Milestone: M1 layer editor · Release: v1 · Status: done · Depends on: F04

## Why

Picking a block is the most frequent action after changing a height. In a pack with a hundred mods there are thousands
of blocks, so finding one has to be quick, and the picture has to be enough for players who cannot read.

## What the player gets

A block picker opened from the button showing the selected layer's block, and from the row above the layers and the
seams between them, which add a layer of the block picked there: all the blocks, the game's own first, with a row of the
blocks picked lately and a row of the starred ones above them; the creative inventory's tabs down its side to narrow
them to one; a search by typing and a filter by mod. Clicking one picks it and goes back. It is a screen of its own
rather than a grid beside the column, which has no room for one at the largest GUI scale.

## Acceptance criteria

- [x] Every block in the game's block registry, modded ones included, with its item picture; blocks without an item
  (water, lava, fire and the like) with their side, as the column draws it.
- [x] Search by name and by id, as the player types.
- [x] Filter by mod (namespace), with the mod's name from the loader (`ClientPlatform`).
- [x] A row of the blocks picked lately and a row of the starred ones over the grid, whatever the mod chosen, each as
  many as fit across; a right click stars or unstars; both kept across games in `config/flatgen/blocks.json`.
- [x] Hover shows name, id and mod.
- [x] Hints for blocks that make a poor layer, under the selected layer in the editor: a falling block over air, a
  fluid or the void, a block that is not whole (torches, flowers, water), a block entity. Their pictures come with
  F16.
- [x] Blocks disabled by the world's feature flags (experimental content not turned on) are hidden.

## Design

- `search/rules/Search` (pure, shared with the biome picker of F07 from the start): matching and ranking (exact id,
  name prefix, word prefix, contains), recent and starred ordering, over plain entries of id, name and namespace.
  Unit-tested once for both pickers.
- `blocks/client/BlockPicker`: the block picker, `BlockPickerScreen`, a `client/gui/ShelvedPickerScreen` as F07's biome
  picker is (a `PickerScreen`: the search field, the mod filter and Cancel; shelves down its side), whose cards are
  block icons: its side holds all the blocks, chosen at first, and the kinds, and the grid's rows of recent and starred
  blocks stand over the shelf's, as asked for after shelves of picks were tried in play. Since 2026-11-20 its kinds are
  the creative inventory's tabs of blocks, natural first as most of a flat world's layers are, then building, coloured,
  functional and redstone, each filled by the game for the world's features with the blocks mods put in them, their
  names and pictures the game's own (`mc/BlockCatalog.tabs`); a panel of three agents chose it as the next left-out
  item, as a player who cannot read cannot search but knows those tabs. Only these five tabs are filled, one by one, as
  filling runs mods' code that may want a world or a server's settings the Create World screen has not got: in the test
  pack Astral Sorcery's did, and filling every tab at once left the picker with none. Every block lists the game's own
  blocks first, then the mods', and leaves out mods' blocks with no item, the parts they move, light or mark with, which
  have no picture.
- `blocks/rules/SetBlock`: the edit, one step to undo; `blocks/rules/LayerHints`: the hints from a block's traits,
  which `mc/BlockCatalog` reads from the game; the starred and recent blocks are `search/rules/Picks`.
- The toolkit gains `TextField`, block icons on the `Canvas` and buttons whose label changes; config files share one
  reader, `config/ConfigFiles`.
- The list comes from the block registry of the world being created, filtered by its enabled feature flags, as the
  vanilla presets screen does (`filterFeatures`).
- Recent and starred blocks live in `config/flatgen/blocks.json`, the blocks feature's own file.

## Tests

- Unit: `SearchTest`, `PicksTest`, `BlockRulesTest` (the edit and the hints), `TypedTextTest`,
  `PickerRowsTest`.
- GameTests on both nodes: the picker lists the game's blocks but air; sand, stone, water, a torch and a chest have
  the traits the hints read.
- Played with Blockpilot (`flatgen:blocks`, now `flatgen:picker`, lists the grid's cards): on NeoForge, search
  "glass" put glass first and picking it made the grass layer glass; it came back as recent, a right click starred
  stone, both kept in `blocks.json`; the mod filter showed the loader's name for the mod; sand picked for the bottom
  layer showed the falling hint. On Fabric, andesite picked through a search set the dirt layer.
- Not tried: a modded block (the dev runs have no block mods); blocks without an item drawn in the grid.

Kinds, 2026-11-20, on 1.21.1 NeoForge in a dev client with Blockpilot: the picker opens on the starred blocks, with the
creative inventory's five tabs of blocks down its side, their names and pictures the game's, and Natural Blocks shows
grass, podzol, mycelium, dirt path and dirt first, in the creative inventory's order; on 1.20.1 Forge and 26.3 NeoForge
the side lists the same five tabs and Building Blocks opens on the oak log; a GameTest checks the tabs on every node's
server. Not tried in game: German names, and a mod's blocks in the tabs.

Played on 2026-11-20 in the test pack with alpha.9: the picker was confusing, with no regular blocks to be seen but a
Create Aeronautics block and blocks without pictures. The log showed why: Astral Sorcery's creative tab code wanted its
server settings, the filling of every tab at once failed, and the picker fell back to every block in the registry's
order. Fixed as above; tried on 1.21.1 NeoForge, where the side starts with Natural Blocks and Every block with stone.
Not tried yet: the pack itself.

Layout, 2026-11-20, as the owner asked: all the blocks chosen at first with rows of the recent and the starred over them
and the tabs to switch to down the side; the cards' words kept clear of the focus ring; Add layer on top opening the
picker first. Tried on 1.21.1 NeoForge: the picker opens on All blocks with Recent and Starred rows, picking glass from
Add layer on top adds a glass layer on top, cancelling adds nothing, and Undo takes the glass back.
