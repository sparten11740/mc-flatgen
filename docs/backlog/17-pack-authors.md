# F17 Pack author support

Milestone: M4 finish and release v1 · Release: v1 · Status: doing · Depends on: F12, F13

## Why

Modpack authors decide which mods reach most players. A pack built around flat worlds (airships, Create builds,
minigames) wants its own presets and its own default without asking players to set anything up.

## What the player gets

Nothing to do: a pack's presets are in the library and its default is set when the pack is first started.

## Acceptance criteria

- [ ] Presets shipped in a mod or resource pack at `assets/<namespace>/flatgen/presets/*.json` show as built-ins of
  that pack (F13).
- [ ] A pack ships `config/flatgen/library.json` with its default preset, "start on Superflat" and hidden built-ins;
  a player's own changes later are kept (the file is the player's once written).
- [x] A page for pack authors in `docs/pack-authors.md` with the file format and examples.

## Design

- Pack presets are F13's `PackPresets` source, which already reads `assets/<namespace>/flatgen/presets/` from every mod
  and resource pack; nothing in the library changes. Their keys are `pack:<namespace>:<name>`, which a pack's
  `library.json` names its default and favourites by.
- A pack's `library.json` is the player's own choices file (F12), so a pack ships it as it ships any default config:
  every key may be left out and takes its default, unknown keys are ignored, and the player's first change writes the
  file as theirs. The page tells pack authors to ship it so that an update does not overwrite it.

## Tests

- Unit: a `library.json` as `docs/pack-authors.md` shows it reads as the page says, keys left out and unknown ones
  included (`ChoicesTest`); Flatgen's own starters, read by the same source, read without problems
  (`FlatgenStarterPresetsTest`).
- Dev client: a resource pack with a preset shows it, and its `library.json` default is ready on Superflat.
