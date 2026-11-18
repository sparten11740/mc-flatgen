# F18 Other flat world types

Milestone: M4 finish and release v1 · Release: v1 · Status: todo · Depends on: F01

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

- NeoForge's event registers editors by key at start-up, before world types are known, so Flatgen's table gains a
  second kind of entry: a test on the selected world type. NeoForge then needs the same small mixin into
  `WorldCreationUiState#getPresetEditor` that Fabric has from F01; it moves to code both loaders share, and the
  NeoForge event stays for `minecraft:flat`.
- `PresetEditors` holds both kinds behind one lookup, so F01's code does not change.

## Tests

- Dev client on NeoForge with Create Aeronautics in the dev run: Airship Ready → Customize opens Flatgen; the created
  world has Flatgen's layers and Aeronautics' game rules.
