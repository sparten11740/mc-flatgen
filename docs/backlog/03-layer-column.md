# F03 Layer column

Milestone: M1 layer editor · Release: v1 · Status: done · Depends on: F01, F02, F26

## Why

The vanilla screen lists layers as text rows with numbers. A drawn column shows the world from the side, so a player
sees how deep each layer is and where the surface ends up without reading a number. It is also the base the editing of
F04 works on.

## What the player gets

On the left of Flatgen's screen, the stack drawn from the side with block textures: bedrock at the bottom, grass on
top, each layer as tall as it is deep, Y numbers at each boundary and lines for the heights that matter.

## Acceptance criteria

- [x] Each layer drawn with its block's side, tiled (the north faces of its model, each tinted as the game tints it;
  the still sprite for fluids, tinted with the biome's water colour; air for an unknown block).
- [x] Heights proportional, with a minimum so a 1-block layer stays visible and clickable. Break marks for very deep
  layers were dropped: with the minimum and scrolling, even a 380-block layer beside a 1-block one stays readable.
- [x] Y numbers at the lines between layers: the surface and the world's floor always, the others where they do not
  crowd a number already there; the selected layer's Ys and the surface in the panel beside the column.
- [x] Marker lines at Y=40 (slime chunks spawn below it) and Y=80 (temperature drops above it, so cool biomes may
  snow) where they fall within the stack. Which matter for the current biome, and the world's bottom and build limit,
  come with F08's facts.
- [x] Click selects a layer (outline), hover shows block name, height and Y range.
- [x] Fits from the smallest window to large GUI scales; scrolls with the wheel when even the minimums cannot fit.
- [x] The editor is rebuilt on the toolkit (`docs/plan.md` 3.3): `FlatgenScreen` is the only class extending `Screen`,
  and only `McCanvas` and `mc/BlockSides` draw through the game's GUI classes; features draw through `Canvas`.

## Design

- `layers/ColumnLayout` (pure, `layers/rules`): from layer heights and the space available, the pixel span of each
  layer, the breaks and the marker positions. Unit-tested.
- `layers/client/ColumnWidget`: draws the layout through Flatgen's `Canvas` (`docs/plan.md` 3.3), never directly
  through the game's drawing classes, which differ by version.
- `mc/BlockSides`: a block's north-face quads with their tints, or its particle sprite when it has none (fluids),
  looked up once per screen and tiled at 16 GUI pixels.
- The toolkit's first pieces: `Rect`, `Canvas`, `Widget`, `McCanvas`, `FlatgenScreen`, `TextButton`.

## Version notes

- 1.21.6 changed GUI drawing, 26.1 renamed `GuiGraphics` to `GuiGraphicsExtractor`; both stay behind `Canvas`.

## Tests

- Unit: `ColumnLayoutTest` (1, 4, 30 and 300 layers, minimum heights, exact filling, scrolling, the layer at a pixel,
  lines inside and outside the stack), `ColumnRowsTest`.
- Played with Blockpilot on NeoForge at GUI scales 4 and 2 (the classic flat, the 104-block tall flat typed into the
  game's presets box, Snowy Kingdom, Water World) and on Fabric at scale 4 (Water World): the column draws each
  stack, a click selects a layer and the panel and `flatgen:editor` follow, the tooltip shows on hover, and
  `flatgen:column` lists each layer's block, Ys and bounds matching the drawing.
- Not tried in game: a stack tall enough to scroll (more than about 23 layers at scale 4); the layout's scrolling is
  covered by the unit tests.
