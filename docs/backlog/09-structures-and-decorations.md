# F09 Structures and decorations

Milestone: M2 biome and world · Release: v1 · Status: doing · Depends on: F03

## Why

A superflat world can have villages, strongholds and other structures, the biome's trees and flowers, and lakes. The
vanilla screen only sets these through presets; FlatEdit+ on 1.21.1 knows only 19 vanilla structure sets, so modded
structures (Underwater Village, IDAS and others in the test pack) cannot be chosen at all.

## What the player gets

A structures panel listing every structure set the game knows, grouped by mod, each switched on or off, with a mark on
those that cannot appear in the chosen biome. Switches for decorations and lakes.

## Acceptance criteria

- [ ] Every structure set in the registry of the world being created, grouped by mod.
- [ ] Three choices: what the biome allows (vanilla's "no overrides"), none, or a chosen list.
- [ ] A structure set whose structures cannot generate in the chosen biome is marked, with the reason as a tooltip.
- [ ] Decorations (the biome's features: trees, flowers, ores) and lakes, each a switch.
- [ ] All three are undoable edits and part of the preset (F02).

## Design

- `structures/rules/StructureChoice` (pure): the three-way choice and the chosen ids.
- `mc/Structures` (in `src/main` until a version needs it to differ, plan 3.2): lists structure sets with their
  structures' allowed biomes, from the registries.
- The panel is built on the toolkit's picture switch (`client/gui/Toggle`), made for F27's options panel.
- A world option (F27) whose effect adds structure sets shows them as chosen here too, so the two never disagree.

## Version notes

- `StructureSet` and `Structure.biomes()` are stable from 1.20.1 to 1.21.1; check 26.1.

## Tests

- Unit: the choice's three states and its conversion to `structure_overrides`.
- GameTest: the structure sets the game's own flat generator keeps for a plains preset (its
  `ChunkGeneratorStructureState`) are exactly those Flatgen marks as able to appear there, for the biome's choice;
  a chosen list keeps villages but not desert pyramids, and none keeps none (`StructureGameTests`). Searching chunks
  for a village, as planned at first, was left out: it needs chunks generated with the preset's settings, which the
  GameTest world does not have, and the generator's own list is what decides it.
