# F01 Customize opens Flatgen

Milestone: M0 foundation · Release: v1 · Status: done · Depends on: F00

## Why

Everything Flatgen does happens in the screen behind Create World → World → World Type: Superflat → Customize. This
feature puts Flatgen's screen there on both loaders without changing anything else on the Create World screen, which
other mods hook into (Create Aeronautics' world types add a tooltip and game rules through the World tab).

## What the player gets

Superflat → Customize opens Flatgen instead of the vanilla flat screen. In this first step it shows the layers of the
current settings, Done applies them, Cancel leaves everything as it was, and a button opens the vanilla editor for
anything Flatgen does not do yet.

## Acceptance criteria

- [x] On `1.21.1-neoforge` and `1.21.1-fabric`, Superflat → Customize opens Flatgen's screen.
- [x] Done writes the settings back to the Create World screen; a world created afterwards has exactly those layers
  and that biome (checked in `level.dat` or in game).
- [x] Cancel and Escape return to Create World with nothing changed.
- [x] "Vanilla editor" opens Minecraft's own flat screen, whose Done also reaches Create World.
- [x] `config/flatgen/flatgen.json` with `"enabled": false` gives the vanilla screen back.
- [x] The World tab's layout and the Create World screen itself are untouched (no mixin into either).
- [x] Other world types are unaffected: Default, Large Biomes, Amplified and Single Biome behave as without Flatgen.

## Design

- `editor/client/PresetEditors`: Flatgen's table of world types it edits and the editor it hands the game, which
  each loader's platform half registers. For now one entry, `minecraft:flat`.
- NeoForge (`platform/neoforge`): on the mod bus, `RegisterPresetEditorsEvent.register(key, editor)` for each entry,
  which replaces the vanilla editor. No mixin.
- Fabric (`platform/fabric`): Fabric API has no such event; a mixin at the head of
  `WorldCreationUiState#getPresetEditor` returns Flatgen's editor when the selected world type is in the table.
- The screen reads the current settings from `WorldCreationContext.selectedDimensions().overworld()` (a
  `FlatLevelSource`, or the default flat settings when it is not) and applies its result with
  `CreateWorldScreen.getUiState().updateDimensions((registries, dimensions) ->
  dimensions.replaceOverworldGenerator(registries, new FlatLevelSource(settings)))`, as the vanilla editor does.
  This reading and writing is the game's API most likely to change, so it lives in `mc/WorldCreation`, in `src/main`
  while every version built shares it.
- The vanilla editor stays reachable through `PresetEditor.EDITORS`, which still holds it on both loaders in 1.21.1.

## Version notes

- 1.21.1 NeoForge patches `getPresetEditor` to look up `PresetEditorManager`; the event is the supported way in.
- 26.1: check `WorldCreationUiState`, `PresetEditor` and NeoForge's event still exist under these names.
- 1.20.1 Forge: check whether Forge 47 has the event; otherwise the Fabric mixin serves Forge too.

## Tests

- Unit: `SettingsTest` reads and writes the settings file's JSON, with defaults for missing keys and values of the
  wrong type.
- Played on `1.21.1-neoforge` and `1.21.1-fabric` with Blockpilot 1.4.0 (`-Pflatgen.blockpilot=1.4.0`), each:
  Superflat → Customize opens Flatgen; Cancel and Escape return unchanged; Vanilla editor opens the game's screen and
  its Cancel returns to Create World; a vanilla preset chosen there (Tunnelers' Dream on NeoForge, Water World on
  Fabric) shows in Flatgen, and after Flatgen's Done the created world's `level.dat` holds exactly its layers, biome
  and structures; with `"enabled": false` Customize opens the vanilla screen without a restart; Large Biomes and
  Amplified keep Customize off and Single Biome opens its own screen.
- Seen at GUI scale 4 on the smallest window: the three footer buttons fit (they did not at the game's default width,
  now 100 pixels each).
- Not tried: with Create Aeronautics present (comes with F18 and the boot in the real pack, F20).
