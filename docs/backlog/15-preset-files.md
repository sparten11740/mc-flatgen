# F15 Sharing preset files

Milestone: M3 presets · Release: v1 · Status: doing · Depends on: F11

## Why

Players share presets with friends and between instances. Minecraft has no file dialog, so sharing has to work
through what the game offers: the clipboard, dropping files on the window and opening folders.

## What the player gets

Copy a preset as text and paste one from text; drop preset files onto Flatgen to import them; a button opens the
presets folder.

## Acceptance criteria

- [ ] Copy preset: its JSON to the clipboard. Paste preset: reads JSON or a vanilla superflat code (F06) from the
  clipboard into a new library preset.
- [ ] Dropping one or more `.json` preset files on Flatgen's editor or its library imports them, with a summary of
  what was imported and what was skipped and why (the editor takes them through its panels, `EditorPanel`).
- [ ] Open presets folder opens `config/flatgen/presets/` in the system's file browser.
- [ ] Imported names that clash get a number ("Tall flat 2"); nothing is overwritten without asking.

## Design

- Reuses F02's reader and F06's parser. An imported preset naming parts this game lacks is kept as it is; the
  library's barrier marks it and using it asks first (F11).
- Dropped files arrive through `Screen.onFilesDrop`; the folder opens through the game's `Util.getPlatform()`.

## Tests

- Unit: paste of JSON, of a vanilla code, of rubbish; name clashes.
- Dev client: copy, paste and folder button on both nodes (dropping files is tried by hand).
