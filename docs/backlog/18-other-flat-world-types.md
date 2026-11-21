# F18 Other flat world types

Milestone: M4 finish and release v1 · Release: v1 · Status: done · Depends on: F01

## Why

Mods add world types that are superflat underneath: Create Aeronautics' "Airship Ready" in the test pack, which has no
Customize button. Flatgen can edit those too.

## What the player gets

Customize is active on any world type whose overworld is superflat, and opens Flatgen there.

## Acceptance criteria

- [x] Any world type whose overworld generator is flat and that has no editor of its own gets Flatgen's editor.
- [x] The world type stays selected, so what its mod does for it (Aeronautics' game rules for Airship Ready) still
  happens.
- [x] World types that change other dimensions (Aeronautics' "End Sea") are left alone unless their overworld is all
  they are.

## Design

- NeoForge's event registers editors by key at start-up, before world types are known, so the choice is made when
  the game asks for the selected type's editor: Fabric's mixin into `WorldCreationUiState#getPresetEditor` from F01
  moves to `editor/client/mixin`, listed by both loaders (`flatgen.mixins.json`), and acts at the method's return,
  after the game or NeoForge's list found its own editor. With it the NeoForge event went too: one way in for both
  loaders is simpler than two that must agree.
- `editor/client/PresetEditors.editorFor` decides: Flatgen's editor for Superflat, and for a type with no editor of
  its own that `mc/WorldTypes.editableWhole` finds Flatgen can edit whole (a flat overworld, and every other
  dimension written as the Default world type writes it, or, from F25, a flat Nether or End, which F22 edits); else
  the editor the game found.
- World options Done set on such a type are taken back when the screen leaves it for a type Flatgen does not edit, as
  they are when it leaves Superflat (F12's watcher asks `PresetEditors.mayHaveEdited`).

## Tests

- GameTest: Superflat, a fixture world type with only a flat overworld and one with every dimension flat (F25) are told
  as ones Flatgen can edit whole; Default, Large Biomes and a fixture type that also changes the End are not
  (`WorldTypeGameTests`).
- Dev client: the fixture's "Airship Ready" (in the dev runs' world types) → Customize opens Flatgen, and the created
  world keeps the type with Flatgen's layers; its "End Sea" keeps Customize greyed. With Create Aeronautics, in the test
  instance (F20).

## Tried

Both loaders, in dev clients with Blockpilot: cycling World Type reaches the fixture's "Airship Ready", and Customize
opens Flatgen's editor on its own layers (bedrock, three stone, grass). On Fabric, a superflat code pasted there and
Done return to Create World with Airship Ready still chosen, and the world made has the code's layers (bedrock, four
glass) and the normal End. Not tried in game: the fixture's "End Sea" with Customize greyed (the GameTest tells it
apart), and Create Aeronautics' own Airship Ready with its game rules, which F20's play in the test instance covers.
