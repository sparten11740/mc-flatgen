# F22 Flat Nether and End

Milestone: after v1 · Release: v1.x · Status: todo · Depends on: F20

## Why

Vanilla offers flat Nether and End only from 26.2 ("flat_all_dimensions"), and FlatEdit+ users have asked for it.
On 1.21.1 only datapacks do it today.

## What the player gets

Optional stacks for the Nether and the End in the same editor, each with its own biome.

## Acceptance criteria

- [ ] A preset may carry a Nether stack and an End stack (F02's format gains optional `nether` and `end` parts;
  older files load as overworld only).
- [ ] Applied by replacing those dimensions' generators with flat ones; the world still opens without Flatgen.
- [ ] Portals work: a Nether portal leads to the flat Nether and back; the End has a way back (exit portal and dragon
  handled, as Create Aeronautics' "End Sea" does by setting the dragon fight data).
- [ ] On 26.2 and later, vanilla's own `flat_all_dimensions` is offered as a built-in.

## Tests

- GameTest: the dimensions' chunks have the stacks' blocks.
- Dev client: through a Nether portal and back; into the End and out.
