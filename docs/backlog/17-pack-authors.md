# F17 Pack author support

Milestone: M4 finish and release v1 · Release: v1 · Status: todo · Depends on: F12, F13

## Why

Modpack authors decide which mods reach most players. A pack built around flat worlds (airships, Create builds,
minigames) wants its own presets and its own default without asking players to set anything up.

## What the player gets

Nothing to do: a pack's presets are in the library and its default is set when the pack is first started.

## Acceptance criteria

- [ ] Presets shipped in a mod or resource pack at `assets/<namespace>/flatgen/presets/*.json` show as built-ins of
  that pack (F13).
- [ ] A pack ships `config/flatgen/flatgen.json` with its default preset, "start on Superflat" and hidden built-ins;
  a player's own changes later are kept (the file is the player's once written).
- [ ] A page for pack authors in `docs/pack-authors.md` with the file format and examples.

## Design

- Pack presets are one more `PresetSource` (F13); no change to the library.

## Tests

- Unit: the pack source; config defaults.
- Dev client: a resource pack with a preset shows it.
