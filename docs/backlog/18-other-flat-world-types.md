# F18 Other flat world types

Milestone: M4 finish and release v1 · Release: v1 · Status: doing · Depends on: F01

## Why

Mods add world types that are superflat underneath: Create Aeronautics' "Airship Ready" in the test pack, which
has no Customize button. Flatgen can edit those too.

## What the player gets

Customize is active on any world type whose overworld is superflat, and opens Flatgen there.

## Acceptance criteria

- [ ] Any world type whose overworld generator is flat and that has no editor of its own gets Flatgen's editor.
- [ ] The world type stays selected, so what its mod does for it (Aeronautics' game rules for Airship Ready) still
  happens.
- [ ] World types that change other dimensions (Aeronautics' "End Sea") are left alone unless their overworld is all
  they are.

## Design

- NeoForge's event registers editors by key at start-up, before world types are known, so the choice is made when
  the game asks for the selected type's editor: Fabric's mixin into `WorldCreationUiState#getPresetEditor` from F01
  moves to `editor/client/mixin`, listed by both loaders (`flatgen.mixins.json`), and acts at the method's return,
  after the game or NeoForge's list found its own editor. With it the NeoForge event went too: one way in for both
  loaders is simpler than two that must agree.
- `editor/client/PresetEditors.editorFor` decides: Flatgen's editor for Superflat, and for a type with no editor of
  its own that `mc/WorldTypes.onlyFlatOverworld` finds superflat underneath and nothing more (a flat overworld, and
  every other dimension written as the Default world type writes it); else the editor the game found.

## Tests

- GameTest: Superflat and a fixture world type with only a flat overworld are told as superflat underneath; Default,
  Large Biomes and a fixture type that also changes the End are not (`WorldTypeGameTests`).
- Dev client: the fixture's "Airship Ready" (in the dev runs' world types) → Customize opens Flatgen, and the created
  world keeps the type with Flatgen's layers; its "End Sea" keeps Customize greyed. With Create Aeronautics, in the
  test instance (F20).
