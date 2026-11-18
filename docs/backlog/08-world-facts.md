# F08 World facts

Milestone: M2 biome and world · Release: v1 · Status: todo · Depends on: F07

## Why

No other superflat tool tells players what their world will be like before they create it. The best-known problem of
superflat worlds is slimes ("Superflat World No Slimes" has 3.6 million downloads on Modrinth), and the second is
weather the player did not expect. Both follow from rules Flatgen can work out from the preset.

## What the player gets

A row of pictures under the column: surface height, weather (sun, rain or snow), slimes yes or no, hostile mobs yes or
no. A "no slimes" button fixes slimes in one click when they would spawn.

## Acceptance criteria

- [ ] Surface Y: the Y mobs stand at, world bottom plus the stack's height.
- [ ] Weather at the surface: none when the biome has no precipitation; snow when it is cold enough at the surface,
  rain otherwise. Above Y=80 temperature drops with height (by 0.05 per 40 blocks in 1.21.1, plus noise of up to
  about 8 blocks either way), so a tall stack in a cool biome can turn rain into snow; the fact says "snow from about
  Y=N" when the surface is near that line.
- [ ] Slimes in slime chunks: when the biome's spawn list has slimes and the surface Y is below 40.
- [ ] Surface slimes: when the biome is in `#minecraft:allows_surface_slime_spawns` (swamps) and the surface Y is
  between 51 and 69, at night.
- [ ] Hostile mobs: when the biome's monster spawn list is not empty and mob spawning is on (F10).
- [ ] "No slimes": raises the stack so the surface is at Y=40 (adds height to the thickest layer between the bottom and
  the top one, the bottom one in a stack of two or fewer, one undoable edit), or offers biomes without slimes. (The
  first rule, the thickest below the top two, made the classic flat world's bedrock 101 deep; tried 2026-11-19.)
- [ ] Each fact is a picture with a tooltip; marker lines of F03 show only when they matter for the current biome.
- [ ] The biome picker's cards (F07) show the facts that differ from biome to biome, for the world as the editor has
  it, with their words in the card's tooltip.

## Design

- `facts/rules/WorldFacts` (pure): from `BiomeInfo` (F07), the surface Y and the world rules, the facts. The numbers
  are named constants with the release they come from (`SLIME_CHUNK_MAX_Y = 40`, `COOLING_START_Y = 80`, ...), so a
  version that changes them changes one place.
- Each fact is a class behind `facts/rules/Fact` (plan 3.4), listed in `Facts`: what it shows (an item's picture,
  struck through for "no", and its words) and the line it draws across the column, if any. The panel, the column's
  lines and the biome picker's cards all read the list, so a new fact changes no other code.
- The rules are taken from 1.21.1's `Slime.checkSlimeSpawnRules` and `Biome.getPrecipitationAt`; the GameTest checks
  Flatgen's facts against the game's own methods.

## Version notes

- Check the slime and temperature rules against each port's `Slime` and `Biome`; the GameTest catches a change.

## Tests

- Unit: every fact for plains, desert, snowy plains, swamp, mushroom fields, at surface Y 39, 40, 60, 100.
- GameTest: for those biomes and heights, `WorldFacts` agrees with `Biome.getPrecipitationAt` and with
  `Slime.checkSlimeSpawnRules` on a slime chunk.
