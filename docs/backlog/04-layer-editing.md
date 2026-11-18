# F04 Layer editing

Milestone: M1 layer editor · Release: v1 · Status: todo · Depends on: F03

## Why

Changing layers is the core of the screen and the part the vanilla screen makes hardest: one layer at a time, a
separate screen to add one, no way to reorder. Very good usability here is the main thing players will judge Flatgen
by.

## What the player gets

Layers edited where they are drawn: drag to reorder, drag a boundary to make a layer deeper or shallower, buttons and
keys for everything, undo for every change.

## Acceptance criteria

- [ ] Add a layer above or below the selected one (with the last used block), remove, duplicate.
- [ ] Drag a layer to a new place in the stack; a gap shows where it will land.
- [ ] Drag the top edge of a layer to change its height; the Y numbers follow while dragging.
- [ ] Height of the selected layer: − and + buttons, scroll wheel over the layer (Shift for 10, Ctrl for 100), typed
  number, quick heights (1, 2, 3, 5, 10, 50, 100).
- [ ] "Surface at Y": type the Y the surface should be at; the selected layer's height changes to get there.
- [ ] Undo and redo (buttons, Ctrl+Z, Ctrl+Y and Ctrl+Shift+Z) for every change, including block and biome changes.
- [ ] Keyboard: arrows select, Alt+↑/↓ move, Delete removes, Ctrl+D duplicates, Insert adds.
- [ ] The stack can never get taller than the world; the control that would go over it stops at the top and says
  why with a picture and a tooltip.

## Design

- `layers/rules/LayerStack` and `layers/rules/LayerEdit` (pure): each change is an edit applied to an immutable stack,
  so undo is a list of stacks and every edit is unit-tested without the game.
- `layers/client`: the widgets turn mouse and keys into edits; they never change the stack themselves.
- Mouse and key input reach the widgets through Flatgen's own input types (`docs/plan.md` 3.3), since 1.21.9 changed
  the game's.

## Version notes

- 1.21.9 replaced the mouse and key callbacks' parameters with event records; Flatgen's screen base in each version
  tree translates them.

## Tests

- Unit: every edit, undo and redo sequences, height limits, "surface at Y".
- Dev client (Blockpilot `drag`, `scroll`, `key`, `click`, `screenshot`): reorder, resize and undo on both nodes.
