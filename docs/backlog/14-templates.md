# F14 Templates

Milestone: M3 presets · Release: v1 · Status: doing · Depends on: F11

## Why

The "generator" in Flatgen: some worlds are better described by a few numbers than by layers. A tall flat world is
"ground at Y=40", a water world is "water 30 deep over sand".

## What the player gets

A templates tab with picture cards. Choosing one shows a few sliders; the column and the facts update while sliding;
Use turns the result into the editor's layers, to change further or save.

## Acceptance criteria

- [ ] Tall flat: ground height (surface Y), top block, filler block.
- [ ] Airship field: deep ground to a chosen height with a grass top, no structures, always clear (Create Aeronautics
  style, without needing that mod).
- [ ] Water world: water depth, floor block, floor depth.
- [ ] Void with platform: the void biome with its start platform.
- [ ] Test layers: a chosen number of alternating layers of chosen blocks, for testing machines and contraptions.
- [ ] Live preview through the column and the facts; Use is one undoable edit.

## Design

- `templates/rules/Template`: an interface with its parameters and a pure function from parameter values to a preset.
  Each template is one class; adding a template is adding a class and listing it, nothing else changes.
- The settings panel is generic over a template's parameters (a number with a range, a block, a switch), each kind a
  record of `Parameter` with a visitor, so the screen builds a control per kind without a branch on kinds.

## Tests

- Unit: each template's output for its edge values; every result validates (F02).
- Dev client: each template's sliders on both nodes.
