# F16 Works without reading

Milestone: M4 finish and release v1 · Release: v1 · Status: todo · Depends on: F03 to F15

## Why

Some players are young children who cannot read yet (a rule in Glowline too). Flatgen should be usable from pictures
alone, and the same care makes it quicker for everyone.

## What the player gets

Every button and fact has a picture; text is extra, in tooltips and under pictures. The screen works with the keyboard
alone and with the game's narrator.

## Acceptance criteria

- [ ] Every control has an icon or a picture; no control is text only. A checklist of every control is in this file
  when the feature is done. Text only so far: the editor's biome, block and structures buttons, No slimes, Undo,
  Redo, the code buttons, the layer buttons (Add above, Add below, Copy, Remove) and the pickers' mod filter.
- [ ] Colour is never the only cue (shape and picture too), for colour-blind players.
- [ ] Keyboard: Tab moves between areas, arrows within one, Enter uses, Escape goes back.
- [ ] Narration: every control narrates its name and state through Minecraft's narrator.
- [ ] Works at GUI scale 1 to 4 and at the smallest window size.

## Design

- Icons are sprites in Flatgen's own GUI atlas (`assets/flatgen/textures/gui/sprites/`), drawn through `Canvas`.
- Widgets implement the game's narration through the version tree's screen base.

## Tests

- Dev client screenshots of every screen at GUI scales 1 to 4 on both nodes; keyboard-only run through the main flows.
