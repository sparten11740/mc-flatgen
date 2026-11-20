# F07 Biome picker

Milestone: M2 biome and world · Release: v1 · Status: done · Depends on: F03

## Why

A superflat world has one biome, and it decides more than players expect: rain or snow or neither, grass and water
colour, which mobs spawn, whether slimes come. FlatEdit+ on 1.21.1 makes players type the biome's id; vanilla offers a
plain list. Flatgen shows biomes as pictures with what each one means for the world.

## What the player gets

A biome button showing the current biome as a little landscape; it opens the picker, laid out as round 3 of the
simple editor drew it (`docs/design/simple-editor.md`): the dimension's biome now in the header with the search and the
filter by mod; down the side the starred and the recent biomes, the kinds of biome (usual, forests, snowy, dry, swamps,
water, mountains, caves, Nether, End) and every biome; the side's choice as landscape cards in the middle, each biome
painted in its own sky, grass, leaves and water; and at the right the biome chosen, painted larger, with F08's facts as
pictures beside their words and a button that picks it. A click chooses a biome, a second click or the button picks
it.

## Acceptance criteria

- [x] Every biome in the registry of the world being created, modded and datapack biomes included (Biomes O' Plenty
  and Oh The Biomes We've Gone in the test pack).
- [x] Card: translated name and the biome's grass, foliage, water and sky colour (since round 3 painted as a
  landscape), and the pictures of F08's facts that differ from biome to biome (weather, slimes, monsters) for the world
  as the editor has it (since round 3 in the panel for the biome chosen, and in each card's tooltip).
- [x] Search by name and id; filter by mod; starred biomes first; recently used kept.
- [x] Round 3: kinds of biome down the side, a mod's biomes sorting in by the game's tags as the game's own do;
  landscape cards; the chosen biome's facts in words beside their pictures; picking from the panel or a second click.
- [x] Choosing a biome is one undoable edit (F04); the column follows it (its water tint now, its marker lines with
  F08, which decides which lines matter for the biome).

## Design

- The biome picker is a `PickerScreen` of its own, `biomes/client/BiomePickerScreen`, as the block picker is the
  one-grid `GridPickerScreen`: the sheet, searching, the mod filter, the picks and stars and the grid are the base's and
  `search/rules`'; the side, the cards and the panel the biome picker's.
- The kinds are `biomes/rules/BiomeKinds`, a list of tests on a biome's tags and climate (`BiomeInfo` carries the tags
  `mc/Biomes` reads), each naming the biome that pictures it; a new kind is one more entry.
- How a biome is painted is `biomes/rules/BiomeScenes`: lists of rules on its tags, name and climate pick its sky, its
  ground (grass, sand, the badlands' terracotta bands, mycelium, snow, stone, none for the void) and what stands on it
  (an oak, a birch, a spruce, an acacia, a cherry or a jungle tree, a peak behind, a mushroom, flowers), as rectangles
  in shares of the picture; a new look is one more rule. `biomes/client/BiomeLandscape` paints a scene at any size,
  the editor's small picture of a biome included, so a player who cannot read tells alike biomes apart.
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

Round 3, 2026-11-20, on 1.21.1 NeoForge in a dev client with Blockpilot: the picker opens on the usual biomes with
Plains chosen, Forests shows the forest biomes, a click on Jungle chooses it and the panel shows it with its facts in
words, a second click picks it and the editor's button says Jungle. Not tried in game: the other nodes, searching and
the mod filter in the new layout, and modded biomes sorting into the kinds.
