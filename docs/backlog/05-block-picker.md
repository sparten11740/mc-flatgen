# F05 Block picker

Milestone: M1 layer editor · Release: v1 · Status: todo · Depends on: F04

## Why

Picking a block is the most frequent action after changing a height. In a pack with a hundred mods there are thousands
of blocks, so finding one has to be quick, and the picture has to be enough for players who cannot read.

## What the player gets

A grid of block pictures beside the column: type to search, narrow it to one mod, see the blocks used recently and the
ones starred. Clicking one sets the selected layer's block.

## Acceptance criteria

- [ ] Every block in the game's block registry, modded ones included, with its item picture; blocks without an item
  (water, lava, fire and the like) with their texture.
- [ ] Search by name and by id, as the player types.
- [ ] Filter by mod (namespace), with the mod's name.
- [ ] Recently used blocks first (last 16), starred blocks in their own row; both kept across games.
- [ ] Hover shows name, id and mod.
- [ ] Hints with a picture for blocks that make a poor layer: falling blocks over air or a fluid, blocks that need
  support (torches, flowers), block entities.
- [ ] Blocks disabled by the world's feature flags (experimental content not turned on) are hidden.

## Design

- `search/rules/Search` (pure, shared with the biome picker of F07 from the start): matching and ranking (exact id,
  name prefix, word prefix, contains), recent and starred ordering, over plain entries of id, name and namespace.
  Unit-tested once for both pickers.
- `blocks/client/BlockGrid`: the grid, through `Canvas`.
- The list comes from the block registry of the world being created, filtered by its enabled feature flags, as the
  vanilla presets screen does (`filterFeatures`).
- Recent and starred blocks live in `config/flatgen/flatgen.json`.

## Tests

- Unit: `Search` ranking, filtering, recents.
- Dev client: search finds `stone`, `create:`, a block without an item; screenshot on both nodes.
