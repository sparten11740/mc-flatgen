# F07 Biome picker

Milestone: M2 biome and world · Release: v1 · Status: todo · Depends on: F03

## Why

A superflat world has one biome, and it decides more than players expect: rain or snow or neither, grass and water
colour, which mobs spawn, whether slimes come. FlatEdit+ on 1.21.1 makes players type the biome's id; vanilla offers a
plain list. Flatgen shows biomes as pictures with what each one means for the world.

## What the player gets

A biome button showing the current biome; it opens a grid of biome cards with search, a filter by mod and starred
biomes. Each card shows the biome's colours and the facts of F08 as small pictures.

## Acceptance criteria

- [ ] Every biome in the registry of the world being created, modded and datapack biomes included (Biomes O' Plenty
  and Oh The Biomes We've Gone in the test pack).
- [ ] Card: translated name, swatches of grass, foliage, water and sky colour, and the fact pictures of F08.
- [ ] Search by name and id; filter by mod; starred biomes first; recently used kept.
- [ ] Choosing a biome is one undoable edit (F04) and updates the column's marker lines.

## Design

- Searching reuses `search/rules/Search` from the block picker (F05); no search code of the biome picker's own.
- `biome/client/BiomeGrid` draws through `Canvas`; the card reads a `BiomeInfo` (pure record: id, name, colours,
  climate, spawns) that the version tree builds from `Biome` (`mc/Biomes`).
- Colours from `BiomeSpecialEffects` (grass and foliage colour overrides or the climate's default colour map).

## Version notes

- `Biome`'s climate and effects accessors are stable from 1.20.1 to 1.21.1; check 26.1.

## Tests

- Unit: `BiomeInfo` building from plain values (search is covered by F05's tests).
- Dev client: the picker lists modded biomes when the pack's biome mods are present (NeoForge node with Biomes O'
  Plenty in the dev run).
