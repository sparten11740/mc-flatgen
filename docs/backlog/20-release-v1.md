# F20 Release v1

Milestone: M4 finish and release v1 · Release: v1 · Status: todo · Depends on: F00 to F19

## Why

v1 is done when players can install it: jars for `1.21.1-neoforge` and `1.21.1-fabric`, tried in a real pack, with a
page that says what Flatgen does.

## Acceptance criteria

- [ ] Every v1 feature done; the milestone reviews of M0 to M4 done and their findings fixed.
- [ ] Boot test: a dedicated server of each loader starts with the jar in its mods folder and is unaffected (client
  mod), and a client of each loader starts with it (`tools/boot.mjs`, as in Glowline).
- [ ] Played in the test instance (NeoForge, about 105 mods) with Create Aeronautics, Hydrol and TerraBlender
  present: Customize, editing, presets, a world created and loaded. Copying the jar into the instance is asked for
  first.
- [ ] GitHub Actions: build and release workflows as in Glowline, run on request.
- [ ] README with screenshots; Modrinth and CurseForge pages; changelog from the `feat` and `fix` commit subjects.
- [ ] Version `1.0.0`, tag `v1.0.0`.

## Tests

- `./gradlew build` on both nodes, boot test, Blockpilot play tests of the main flows on both nodes.
