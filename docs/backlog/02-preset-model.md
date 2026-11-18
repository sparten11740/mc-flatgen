# F02 Preset model and file format

Milestone: M1 layer editor · Release: v1 · Status: todo · Depends on: F00

## Why

Every other feature works on a preset: the editor changes one, the library stores them, templates make them, the facts
read them. One plain model, the same on every Minecraft version and testable without the game, keeps those features
apart from each other and from the game's own classes, which change between versions.

## What the player gets

Nothing directly; presets saved by one version of Flatgen load in every later one, on every Minecraft version.

## Acceptance criteria

- [ ] `preset/rules/Preset`: name, layers (block id and height, bottom first), biome id, structures (either "what the
  biome allows" or a chosen list of structure set ids), decorations on or off, lakes on or off, world options (F27) by
  id with their values, icon block id. Ids are plain text, so a preset naming a block, biome or option from a mod
  that is not installed still loads.
- [ ] Validation: total height within the world's height (read from the dimension, not hard-coded 384), heights from
  1, no unknown ids when applying (reported, never dropped silently). An empty layer list is valid, an empty world,
  as Create Aeronautics' "End Sea" uses it.
- [ ] JSON format with `"format": 1`; unknown keys ignored, missing keys get defaults, so older and newer files load.
- [ ] Converting to and from `FlatLevelGeneratorSettings` (version tree, `mc/FlatSettings`), keeping the structure
  overrides and lake features the settings had.
- [ ] Round trip: settings → preset → settings gives equal settings for every vanilla flat preset.

## Design

- Pure in `preset/rules`: no Minecraft classes, Gson for JSON (Minecraft ships it on every version).
- The game-facing half (`mc/FlatSettings` in each version tree) resolves ids against the registries of the world being
  created (`WorldCreationContext.worldgenLoadContext()`), so datapack and modded blocks, biomes and structures resolve.
- Vanilla's settings: `layers`, `biome`, `lakes` (bool), `features` (bool, the biome's decorations),
  `structure_overrides` (absent means every structure set the biome allows).
- File example:

  ```json
  {
    "format": 1,
    "name": "Tall flat, no slimes",
    "icon": "minecraft:grass_block",
    "layers": [
      { "block": "minecraft:bedrock", "height": 1 },
      { "block": "minecraft:stone", "height": 100 },
      { "block": "minecraft:dirt", "height": 2 },
      { "block": "minecraft:grass_block", "height": 1 }
    ],
    "biome": "minecraft:plains",
    "structures": ["minecraft:strongholds", "minecraft:villages"],
    "decorations": false,
    "lakes": false,
    "options": { "flatgen:always_day": true, "flatgen:always_clear": true }
  }
  ```

## Version notes

- `FlatLevelGeneratorSettings`, `FlatLayerInfo` and their codec are stable from 1.20.1 to 1.21.1; check 26.1.
- `ResourceLocation` is `Identifier` from 1.21.11 (rename rule).

## Tests

- Unit: JSON read and write, defaults for missing keys, unknown keys ignored, validation messages.
- GameTest: each vanilla flat preset converts to a preset and back unchanged; a preset applied to a level generates a
  chunk whose blocks match its layers at each Y.
