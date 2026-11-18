# F13 Built-in presets

Milestone: M3 presets · Release: v1 · Status: todo · Depends on: F11

## Why

A new player has no presets yet. The vanilla presets and a few of Flatgen's own give a start, and modded flat presets
from datapacks belong in the same place.

## What the player gets

The library opens with the vanilla presets (Classic Flat, Tunnelers' Dream, Water World and the rest), any flat
presets other mods or datapacks add, and Flatgen's starters such as "Tall flat, no slimes". Built-ins can be applied
and duplicated, not changed or deleted.

## Acceptance criteria

- [ ] Vanilla's and datapacks' flat presets from the registry of the world being created
  (`flat_level_generator_preset`, those in `#minecraft:visible`), with their own icons and names.
- [ ] Flatgen's starters from resources at `assets/<namespace>/flatgen/presets/*.json`, so mods and resource packs can
  add their own (used by F17): "Tall flat, no slimes" (bedrock 1, stone 100, dirt 2, grass 1), "Airship field",
  "Glass floor" and "Void with platform".
- [ ] Built-ins marked as such; duplicate makes an editable copy in the library.
- [ ] A setting hides built-ins (all, or vanilla's only).

## Design

- Built-ins are a second source behind the same `PresetSource` interface the user folder uses, so the library lists
  sources without knowing their kind; a new source (F17's pack presets) is a new class, not a change to the library.
- `mc/FlatPresets` (version tree) turns registry presets into Flatgen presets through F02's conversion.

## Tests

- Unit: sources merged in order, read-only built-ins.
- GameTest: every registry flat preset converts without problems.
