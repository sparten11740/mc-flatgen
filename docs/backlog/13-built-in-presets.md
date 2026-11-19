# F13 Built-in presets

Milestone: M3 presets · Release: v1 · Status: done · Depends on: F11

## Why

A new player has no presets yet. The vanilla presets and a few of Flatgen's own give a start, and modded flat presets
from datapacks belong in the same place.

## What the player gets

The library opens with the vanilla presets (Classic Flat, Tunnelers' Dream, Water World and the rest), any flat
presets other mods or datapacks add, and Flatgen's starters such as "Tall flat, no slimes". Built-ins can be applied
and duplicated, not changed or deleted.

## Acceptance criteria

- [x] Vanilla's and datapacks' flat presets from the registry of the world being created
  (`flat_level_generator_preset`, those in `#minecraft:visible`), with their own icons and names.
- [x] Flatgen's starters from resources at `assets/<namespace>/flatgen/presets/*.json`, so mods and resource packs can
  add their own (used by F17): "Tall flat, no slimes" (bedrock 1, stone 100, dirt 2, grass 1), "Airship field",
  "Glass floor" and "Void with platform".
- [x] Built-ins marked as such; duplicate makes an editable copy in the library.
- [x] A setting hides built-ins (all, or vanilla's only).

## Design

- Built-ins are sources behind the same `library/PresetSource` interface the player's folder (`PresetStore`) uses,
  listed in `LibrarySources`, so the library lists sources without knowing their kind; a new source is a new class
  there, not a change to the library. Each preset has a key: a player's file name, `game:<id>` for the game's and
  data packs' (`GamePresets`), `pack:<id>` for files of mods and resource packs (`PackPresets`, Flatgen's starters
  among them, their names translation keys); favourites and the default name presets by key, so built-ins can be
  either.
- `mc/FlatPresets` (in `src/main` until a version needs it to differ) turns the registry's visible presets into
  Flatgen presets through F02's conversion, with the name the game's presets screen shows and its item as icon.
- The library orders the player's own first, then the game's, then packs'; a built-in carries a book mark and can be
  used and copied (into the player's folder) but not renamed, saved over or deleted. The library's Built-ins button
  shows all, all but the game's, or none, kept in library.json.

## Tests

- Unit: sources merged in order, read-only built-ins.
- GameTest: every registry flat preset converts without problems.

## Tried

Both loaders, in dev clients with Blockpilot: the library lists the game's flat presets (Classic Flat, Bottomless Pit,
Desert, Overworld, Redstone Ready and the rest, the void preset's air left empty) with their items and book marks
after the player's own. Not tried in game: Flatgen's starters, further down the grid than the screenshots reached
(a unit test reads them), copying a built-in, the Built-ins button's other settings, and a mod's or resource pack's
own presets.
