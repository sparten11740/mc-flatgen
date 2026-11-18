# F07 Biome picker

Milestone: M2 biome and world · Release: v1 · Status: done · Depends on: F03

## Why

A superflat world has one biome, and it decides more than players expect: rain or snow or neither, grass and water
colour, which mobs spawn, whether slimes come. FlatEdit+ on 1.21.1 makes players type the biome's id; vanilla offers a
plain list. Flatgen shows biomes as pictures with what each one means for the world.

## What the player gets

A biome button showing the current biome; it opens a grid of biome cards with search, a filter by mod and starred
biomes. Each card shows the biome's colours and the facts of F08 as small pictures.

## Acceptance criteria

- [x] Every biome in the registry of the world being created, modded and datapack biomes included (Biomes O' Plenty
  and Oh The Biomes We've Gone in the test pack).
- [x] Card: translated name, swatches of grass, foliage, water and sky colour; F08 adds its fact pictures.
- [x] Search by name and id; filter by mod; starred biomes first; recently used kept.
- [x] Choosing a biome is one undoable edit (F04); the column follows it (its water tint now, its marker lines with
  F08, which decides which lines matter for the biome).

## Design

- The biome picker is the toolkit's `PickerScreen` and `CardGrid`, as the block picker is since this feature, with
  cards of its own (`biomes/client/BiomePicker`); searching, the mod filter and the starred and recent sections are
  theirs and `search/rules`', with no code of the biome picker's own.
- Recent and starred biomes live in `config/flatgen/biomes.json`, the biome feature's own file (`search/PicksFile`).
- The card reads a `BiomeInfo` (pure record: id and colours; F08 adds climate and spawns) that `mc/Biomes` builds from
  each `Biome` of the registries of the world being created.
- Colours from `BiomeSpecialEffects` (grass and foliage colour overrides or the climate's default colour map).

## Version notes

- `Biome`'s climate and effects accessors are stable from 1.20.1 to 1.21.1; check 26.1.

## Tests

- Unit: `BiomeInfo` building from plain values (search is covered by F05's tests).
- Dev client: the picker lists modded biomes when the pack's biome mods are present (NeoForge node with Biomes O'
  Plenty in the dev run).

## Tried

Both loaders, in dev clients with Blockpilot: the editor's biome button opens the picker with every vanilla biome as a
card, searching "swamp" by name and "minecraft:desert" by id finds them, picking one is a step to undo and redo, the
picked biome comes back as recent, a right click stars it (kept in biomes.json), and a world created after Done has
the biome. Not tried: modded or data pack biomes, as the dev runs have no biome mod.
