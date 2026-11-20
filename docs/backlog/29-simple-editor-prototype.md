# F29 A simpler editor a child can work out

Milestone: design track, when the owner asks for it · Release: with the next release · Status: done · Depends on: F06

## Why

The editor as built in M1 works, but it reads like a tool for adults: a row of small buttons, numbers, Y values and
text. The owner wants something that looks simpler and that a child could mostly work out alone. Getting there needs a
design look from outside the code, before more screens are built the same way.

## What the player gets

Nothing yet: a prototype on its own branch, presented to the owner with screenshots, to be accepted, changed or dropped.

## Acceptance criteria

- [x] A panel of agents reviews the editor as it is (screenshots at GUI scales 4 and 2, and the flows: open, change a
  layer, pick a block, undo, done), each from its own view:
  - a user-experience expert in designing for children, who leads the panel;
  - a child of about six who cannot read yet, played by an agent told to judge by pictures, colours and sounds only;
  - a Minecraft player who builds flat worlds often (the test instance's habits);
  - an accessibility expert (colour, size, keyboard, narration).
- [x] The panel's findings and a proposed design are written up in `docs/design/simple-editor.md`: what a child sees
  first, which controls stay, which hide behind an "everything" view, pictures for every action, bigger targets,
  fewer numbers.
- [x] A prototype of that design on the branch `prototype/simple-editor`, built on the same toolkit and edits, so the
  existing logic is reused rather than copied.
- [x] Presented to the owner: screenshots of each screen and a short walk through the flows, with what was left out and why.
- [x] Nothing of it reaches `main` until the owner has decided; what is accepted flows into F16 (works without reading) and
  the later screens.

## Design

- The panel runs as separate agents with fresh contexts, given the screenshots and the backlog, not the author's
  opinion of them; the UX expert merges their views into one proposal.
- The prototype changes screens only; the editor's state, edits and history (`editor/rules`, `layers/rules`) stay as
  they are, which shows whether the split between logic and screens holds.

## Prototype build (on `prototype/simple-editor`)

The owner chose direction C in the modern look (`docs/design/simple-editor.md`) and asked for it built overnight. The build:

- [x] **Look and scale** for every Flatgen screen: Flatgen GUI pixels of its own (`LayoutScale`, whole window pixels,
  at least 640 by 360, the canvas at a third of 1920 by 1080), Inter text in three styles (`TextStyle`), rounded
  sprites and the palette in `Look`, all written by `tools/modern_look.py`; switches for toggles.
- [x] **Editor regions**: a panel says where it goes (header tools, the bar above the layers, the aside, the footer)
  and whether only Advanced shows it; the screen lays out header (title, dimensions as segments, Presets, Templates,
  Advanced), the world on the left, the layers in the middle, the aside on the right, the footer (Undo, Redo, Cancel,
  Done).
- [x] **Layer cards**: grip to drag, the block with its count, the name (Advanced: Ys and id), thinner and thicker
  (Advanced: typed), copy (Advanced) and remove; the chosen card outlined; scrolling when they do not fit.
- [x] **Empty Nether and End**: the dimension as the game makes it, and one button to make it flat.
- [x] **Block picker as a sheet**, then the library, templates, biomes, structures and questions in the look
  (the library and templates keep their old arrangement in the new frame).
- [x] Both releases built and tried in a dev client; screenshots beside the canvas boards for the owner.

## Decision

On 2026-11-20 the owner accepted the prototype: Flatgen goes on in the modern look. Their first finding, that the time and
weather card's pictures did not say what they mean, was fixed the same day with pictures of Flatgen's own (F16's
table). The prototype reaches `main` when the owner merges its branch.
