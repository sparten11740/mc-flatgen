# F11 Preset library

Milestone: M3 presets · Release: v1 · Status: done · Depends on: F02, F03

## Why

The saves of the test instance are 21 superflat worlds with four layer stacks, entered again and again. Saved
presets end that, and they are the base for favourites, the default and sharing.

## What the player gets

A presets screen of cards, each with a picture of its column and its name. Apply one, save the current settings as a
new preset or over an existing one, rename, duplicate, delete.

Since round 3 of the simple editor (`docs/design/simple-editor.md`) the screen is laid out as that round drew it: Back,
a search, "Save current as new" and a menu of the rest (paste, the folder, server lines, datapack, starting on
Superflat, the built-ins shown) in the header; the player's presets and the built-in ones under their headings as tall
cards, each its layers standing in the sky over its name and, for a built-in, the mod or pack it comes from; and a
footer for the card picked, with its name to change, Rename, Copy, Share, Save over, Delete, the default and Use.

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
- `library/PresetStore`: reads and writes the folder, each file written whole (`config/ConfigFiles`); it is
  constructed with its folder, so its tests use a temporary one.
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

Round 3, 2026-11-20, on 1.21.1 NeoForge in a dev client with Blockpilot: the header, the cards under their headings
with the built-ins' mods on chips, picking a card fills the footer, the menu opens under its button with every row,
Save current as new keeps preset.json picked under the name Preset, Delete asks and removes it, and Back returns to the
editor. Not tried in game: the search, renaming from the footer, the menu's rows and the other nodes.
