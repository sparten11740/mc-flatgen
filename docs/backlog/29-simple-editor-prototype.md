# F29 A simpler editor a child can work out

Milestone: design track, when the owner asks for it · Release: none until approved · Status: todo · Depends on: F06

## Why

The editor as built in M1 works, but it reads like a tool for adults: a row of small buttons, numbers, Y values and
text. The owner wants something that looks simpler and that a child could mostly work out alone. Getting there needs a
design look from outside the code, before more screens are built the same way.

## What the player gets

Nothing yet: a prototype on its own branch, presented to the owner with screenshots, to be accepted, changed or dropped.

## Acceptance criteria

- [ ] A panel of agents reviews the editor as it is (screenshots at GUI scales 4 and 2, and the flows: open, change a
  layer, pick a block, undo, done), each from its own view:
  - a user-experience expert in designing for children, who leads the panel;
  - a child of about six who cannot read yet, played by an agent told to judge by pictures, colours and sounds only;
  - a Minecraft player who builds flat worlds often (the test instance's habits);
  - an accessibility expert (colour, size, keyboard, narration).
- [ ] The panel's findings and a proposed design are written up in `docs/design/simple-editor.md`: what a child sees
  first, which controls stay, which hide behind an "everything" view, pictures for every action, bigger targets,
  fewer numbers.
- [ ] A prototype of that design on the branch `prototype/simple-editor`, built on the same toolkit and edits, so the
  existing logic is reused rather than copied.
- [ ] Presented to the owner: screenshots of each screen and a short walk through the flows, with what was left out and why.
- [ ] Nothing of it reaches `main` until the owner has decided; what is accepted flows into F16 (works without reading) and
  the later screens.

## Design

- The panel runs as separate agents with fresh contexts, given the screenshots and the backlog, not the author's
  opinion of them; the UX expert merges their views into one proposal.
- The prototype changes screens only; the editor's state, edits and history (`editor/rules`, `layers/rules`) stay as
  they are, which shows whether the split between logic and screens holds.
