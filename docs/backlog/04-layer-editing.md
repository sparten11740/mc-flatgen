# F04 Layer editing

Milestone: M1 layer editor · Release: v1 · Status: done · Depends on: F03

## Why

Changing layers is the core of the screen and the part the vanilla screen makes hardest: one layer at a time, a
separate screen to add one, no way to reorder. Very good usability here is the main thing players will judge Flatgen
by.

## What the player gets

Layers edited where they are drawn: drag to reorder, drag a boundary to make a layer deeper or shallower, buttons and
keys for everything, undo for every change.

## Acceptance criteria

- [x] Add a layer above or below the selected one (of the selected layer's block, one block deep; the blocks used
  lately are first in F05's picker), remove, duplicate.
- [x] Drag a layer to a new place in the stack; a line shows where it will land.
- [x] Drag the top edge of a layer to change its height; the Y numbers follow while dragging, and the whole drag is
  one step to undo.
- [x] Height of the selected layer: − and + buttons, scroll wheel over the layer (Shift for 10, Ctrl for 100), typed
  number, quick heights (1, 2, 3, 5, 10, 50, 100). The wheel over the Y numbers scrolls a tall column instead.
- [x] "Surface at Y": type the Y the surface should be at; the selected layer's height changes to get there.
- [x] Undo and redo (buttons, Ctrl+Z, Ctrl+Y and Ctrl+Shift+Z) for every change, including block and biome changes.
- [x] Keyboard: arrows select, Alt+↑/↓ move, Delete removes, Ctrl+D duplicates, Insert adds above the selected layer,
  asking for the block first. On a Mac, Ctrl is the Command key, as the game has it; a number field with the keyboard
  takes its keys first, and Escape gives the keyboard back rather than closing the editor.
- [x] The stack can never get taller than the world; the control that would go over it stops at the top and says
  why in its tooltip ("The world is 384 blocks tall"); the picture comes with F16.

## Design

- `editor/rules`: `EditorState` (the preset and the selected layer), `Edit` (one change, a class per kind, so later
  features add theirs), `History` (undo and redo as a list of states), `World` (the floor and height edits keep to).
- `layers/rules/LayerEdits`: add, duplicate, remove, move, set and change the height, surface at Y; unit-tested.
- `layers/client`: the column turns mouse input into edits through the editor's `EditorContext`, with a dragged change
  previewed until it is let go; widgets never change the preset themselves. The layer controls are the editor panel
  `LayerPanel`.
- The toolkit grows: modifier keys, drags, keyboard and typing for the widget that took a click (`FlatgenScreen`),
  `NumberField`, buttons that can be unusable with a tooltip saying why, `TextLines`, `FlowLayout`, and widgets that
  describe themselves for Blockpilot's `flatgen:widgets`.

## Version notes

- 1.21.9 replaced the mouse and key callbacks' parameters with event records; Flatgen's screen base in each version
  tree translates them.

## Tests

- Unit: `LayerEditsTest` (every edit, height limits, surface at Y, empty stacks), `HistoryTest`, `WidgetRowsTest`.
- Played with Blockpilot on NeoForge: +, a quick height, a typed height, surface at Y 40, undo and redo, add above,
  copy, remove, the keys (up, Alt+down, Delete, Insert, Command+Z), dragging a layer's edge, dragging a layer to the
  top, and the wheel alone, with Shift and with Command, checking `flatgen:editor` after each; Done and Create New
  World made a world whose `level.dat` holds exactly the edited stack. A shorter run on Fabric did the same.

2026-11-21: adding a layer as round 4 of the design canvas drew it, direction A, which the owner chose after a young
player missed, several times, that the blue Add layer on top button in the bar added layers. Above the cards stands a
row not there yet, as large as a card: a dashed edge, a chequered placeholder block as large as a block's picture with a
plus, and New layer on top (New layer on an empty stack), with no tooltip as its words say it. It and Insert open the
block picker first; cancelling adds nothing, and a full world greys it and says why. Tried on 1.21.1 NeoForge: the row
lit under the mouse, a layer added from it, Insert opening the picker, cancelling adding nothing, and an empty stack
showing New layer alone.

The design also had a seam under every card adding a layer there; built and tried, the owner found it bulky even
quieted, and it was taken out again.

Copy, in Advanced, keeps its picture, and while the mouse is on it the copy is drawn faint in the place above the card,
where it will go, which the owner found clearer than a tooltip describing it. Tried on 1.21.1 NeoForge.
