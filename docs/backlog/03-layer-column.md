# F03 Layer column

Milestone: M1 layer editor · Release: v1 · Status: todo · Depends on: F01, F02, F26

## Why

The vanilla screen lists layers as text rows with numbers. A drawn column shows the world from the side, so a player
sees how deep each layer is and where the surface ends up without reading a number. It is also the base the editing of
F04 works on.

## What the player gets

On the left of Flatgen's screen, the stack drawn from the side with block textures: bedrock at the bottom, grass on
top, each layer as tall as it is deep, Y numbers at each boundary and lines for the heights that matter.

## Acceptance criteria

- [ ] Each layer drawn with its block's texture (side face for blocks, the still sprite for fluids, a placeholder for
  blocks without a model or from a missing mod).
- [ ] Heights proportional, with a minimum so a 1-block layer stays visible and clickable; very deep layers shortened
  with a break mark and their real height written on them.
- [ ] Y numbers at every boundary and at the surface (where mobs stand).
- [ ] Marker lines: world bottom and build limit, Y=40 (slime chunks spawn below it), Y=80 (temperature drops above
  it, so cool biomes may snow). Lines show only when they matter for the current biome (F08 supplies that; until
  then always).
- [ ] Click selects a layer (highlight), hover shows block name and Y range.
- [ ] Fits from the smallest window Minecraft allows to large GUI scales; scrolls when it cannot fit.
- [ ] The editor is rebuilt on `client/gui/FlatgenScreen` and `Canvas` (`docs/plan.md` 3.3): afterwards nothing in
  `src/main` extends `Screen` or draws through the game's GUI classes. F01's `EditorScreen` uses 1.21.1 layout classes
  that 1.20.1 lacks; it becomes the column editor's screen.

## Design

- `layers/ColumnLayout` (pure, `layers/rules`): from layer heights and the space available, the pixel span of each
  layer, the breaks and the marker positions. Unit-tested.
- `layers/client/ColumnWidget`: draws the layout through Flatgen's `Canvas` (`docs/plan.md` 3.3), never directly
  through the game's drawing classes, which differ by version.
- Textures from the block's model (`BlockModelShaper` particle sprite or the side quad's sprite); fluids from their
  still texture.

## Version notes

- 1.21.6 changed GUI drawing, 26.1 renamed `GuiGraphics` to `GuiGraphicsExtractor`; both stay behind `Canvas`.

## Tests

- Unit: layout for 1, 4, 30 and 300 layers; minimum heights; break marks; markers inside and outside the world.
- Dev client: screenshots of the vanilla presets and of a 104-block stack on both nodes at GUI scales 1 to 4.
- `flatgen:column` (F26) lists each layer's key, block, height, Y range and bounds, matching the drawing.
