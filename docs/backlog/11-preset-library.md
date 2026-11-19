# F11 Preset library

Milestone: M3 presets · Release: v1 · Status: done · Depends on: F02, F03

## Why

The saves of the test instance are 21 superflat worlds with four layer stacks, entered again and again. Saved
presets end that, and they are the base for favourites, the default and sharing.

## What the player gets

A presets screen of cards, each with a picture of its column and its name. Apply one, save the current settings as a
new preset or over an existing one, rename, duplicate, delete.

## Acceptance criteria

- [x] Cards with a small drawing of the preset's column (reusing F03's column drawing at a small size) and its icon
  block; the name under it.
- [x] Apply (one undoable edit in the editor), save as new, overwrite (asks first), rename, duplicate, delete (asks
  first).
- [x] Stored as one JSON file per preset in `config/flatgen/presets/` (the format of F02), so they can be copied
  between instances; written atomically (temporary file, then rename).
- [x] A preset naming a block, biome or structure set that this game does not have shows a warning picture with the
  missing ids; applying it asks whether to replace the missing parts (with stone, plains, or nothing) or to cancel.
  Nothing is dropped silently.
- [x] A broken file is skipped with a log line and a warning card, never a crash.

## Design

- `library/rules/PresetLibrary` (pure): the list, ordering (favourites, then recent, then by name), naming new presets
  ("Preset 2"), finding missing ids against a set of known ids.
- `library/PresetStore`: reads and writes the folder through a small `Files` seam, so the pure part is tested without
  the disk.
- `library/client/LibraryScreen` reuses the card grid widget the biome picker uses (`client/gui/CardGrid`).

## Tests

- Unit: library ordering, naming, missing ids, broken files, atomic writes (temporary folder).
- Dev client: save, rename, duplicate, delete and apply on both nodes; a preset with a modded block on a node without
  that mod shows the warning.

## Tried

Both loaders, in dev clients with Blockpilot: Presets opens the library; Save new keeps the editor's settings as
sandy.json, Copy makes sandy_2.json (named "Sandy 2"), Rename makes renamed.json, Delete asks first and removes the
file, and Use puts the preset in the editor as a step Undo takes back; cards show the column, the icon and built-ins'
marks. Not tried in game: Save over (asks first, as Delete does), a preset with parts the game lacks and a broken
file, which the unit tests cover.
