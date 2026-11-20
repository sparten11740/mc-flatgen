# F14 Templates

Milestone: M3 presets · Release: v1 · Status: done · Depends on: F11

## Why

The "generator" in Flatgen: some worlds are better described by a few numbers than by layers. A tall flat world is
"ground at Y=40", a water world is "water 30 deep over sand".

## What the player gets

A templates tab with picture cards. Choosing one shows a few sliders; the column and the facts update while sliding;
Use turns the result into the editor's layers, to change further or save.

Since round 3 of the simple editor (`docs/design/simple-editor.md`) the screen is laid out as that round drew it: the
templates down the left as cards, each a picture of what it makes, its name and a line on what it is; the chosen one's
name, what it is and its settings in the middle, numbers as sliders with a step down and up; at the right what it
makes, its ground in its sky with the air above it on a chip, the world's facts and its layers top first; and a footer
saying what Use does, with Cancel and Use.

## Acceptance criteria

- [x] Tall flat: ground height (surface Y), top block, filler block.
- [x] Airship field: deep ground to a chosen height with a grass top, no structures, always clear (Create Aeronautics
  style, without needing that mod).
- [x] Water world: water depth, floor block, floor depth.
- [x] Void with platform: the void biome with its start platform.
- [x] Test layers: a chosen number of alternating layers of chosen blocks, for testing machines and contraptions.
- [x] Live preview through the column and the facts; Use is one undoable edit.

## Design

- `templates/rules/Template`: an interface with its parameters and a pure function from parameter values to a preset.
  Each template is one class; adding a template is adding a class and listing it, nothing else changes.
- The settings panel is generic over a template's parameters (a number with a range, a block, a switch), each kind a
  record of `Parameter` with a visitor, so the screen builds a control per kind without a branch on kinds.

## Tests

- Unit: each template's output for its edge values; every result validates (F02).
- Dev client: each template's sliders on both nodes.

## Tried

Both loaders, in dev clients with Blockpilot: the templates screen shows the templates as pictures, the column's
preview and the facts beside the settings; the tall flat's height slider moves by a click and the wheel (Y 214, then
216), Use puts bedrock, 278 stone and grass in the editor with the surface at Y 216 as one step, and Undo takes it back;
the water world puts sand 5 deep under 30 of water in an ocean, and test layers eight alternating concrete layers. Not
tried in game: the airship field and the void with platform (unit-tested), and choosing the top and filler blocks
through the block picker.

Round 3, 2026-11-20, on 1.21.1 NeoForge in a dev client with Blockpilot: the cards, the tall flat's settings and what it
makes (102 stone under grass at Y 40, 280 blocks of air), the water world's step up to 31 deep, the void's "nothing to
set", and Use puts the tall flat in the editor as one step Undo takes back. Not tried in game: the other nodes, the
airship field's switch and the block picker from a block row.
