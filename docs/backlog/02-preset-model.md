# F02 Preset model and file format

Milestone: M1 layer editor · Release: v1 · Status: done · Depends on: F00

## Why

Every other feature works on a preset: the editor changes one, the library stores them, templates make them, the facts
read them. One plain model, the same on every Minecraft version and testable without the game, keeps those features
apart from each other and from the game's own classes, which change between versions.

## What the player gets

Nothing directly; presets saved by one version of Flatgen load in every later one, on every Minecraft version.

## Acceptance criteria

- [x] `preset/rules/Preset`: name, layers (block id and height, bottom first), biome id, structures (either "what the
  biome allows" or a chosen list of structure set ids), decorations on or off, lakes on or off, world options (F27) by
  id with their values, icon block id. Ids are plain text, so a preset naming a block, biome or option from a mod
  that is not installed still loads.
- [x] Validation: total height within the world's height (`Preset.fitsWorld`, given the dimension's height, not a
  hard-coded 384), heights from 1, no unknown ids when applying (`FlatSettings.missingIds` reports them, never dropped
  silently). An empty layer list is valid, an empty world, as Create Aeronautics' "End Sea" uses it.
- [x] JSON format with `"format": 1`; unknown keys ignored, missing keys get defaults, so older and newer files load.
- [x] Converting to and from `FlatLevelGeneratorSettings` (`mc/FlatSettings`), keeping the structure overrides and
  lake features the settings had.
- [x] Round trip: settings → preset → settings gives equal settings for every vanilla flat preset.

## Design

- Pure in `preset/rules`: no Minecraft classes, Gson for JSON (Minecraft ships it on every version), read through the
  shared lenient reader `json/rules/JsonFields`, which the settings file uses too.
- The game's settings class keeps its decoration and lake switches private, so Flatgen goes through the JSON its codec
  reads and writes: `preset/rules/VanillaSettings` maps a preset to that JSON and back (plain Java, unit-tested), and
  `mc/FlatSettings` only runs the codec. No mixin or accessor is needed.
- `mc/FlatSettings` resolves ids against the registries of the world being created
  (`WorldCreationContext.worldgenLoadContext()`), so datapack and modded blocks, biomes and structures resolve. It
  finds missing ids first, as the game's codec would quietly put plains in place of an unknown biome and every
  structure set in place of an unknown one.
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

- Unit: `PresetTest`, `PresetJsonTest`, `VanillaSettingsTest` (read and write, defaults for missing keys, values of
  the wrong kind noted, unknown keys ignored, unreadable layers left out and named, the game's own settings JSON from
  real level.dat files).
- GameTests on both nodes (`runGameTestServer`, `runGameTest`, run by `./gradlew build`): every superflat preset in the
  game's registry converts to a preset and back to the same settings JSON; the tall flat preset's column generates
  bedrock, stone, dirt and grass from the world's floor up, with air above; ids of missing mods are found. Seen to
  fail when the conversion swaps decorations and lakes.
- GameTests run on a dedicated GameTest server, so both jars load on a server: Fabric's now declares environment `*`,
  as NeoForge's already loaded on servers, applying its mixin on clients only.
