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

## Design

- The preset's `nether` and `end` parts (`preset/rules/Stack`: layers and biome) are optional; `Preset.stack(Dimension)`
  gives the overworld's from the preset's own layers and biome, so everything that reads those keeps working.
- Done replaces the Nether's and the End's generators with superflat ones of their stacks (no structures, decorations
  or lakes) and keeps their dimension types, so portals, the dragon fight and the dimensions' sky stay the game's; a
  dimension without a stack gets its world type's generator back, undoing an earlier Done. Reopening Customize reads the
  stacks back from the screen. A datapack export (F21) carries them too.
- The dragon is left to the game: its fight places the exit portal's podium on the flat surface at the centre and
  spawns the dragon, whose death lights the portal, so the End always has a way back without Flatgen touching the
  fight's data.
- The game puts a player arriving in the End on its obsidian platform at Y 48 (X 100, Z 0), so an End stack 49 blocks
  tall from Y 0 meets it flush.
- The game calls a world with a Nether or End not its own "experimental" and asks once, when the world is created
  (loading it again does not ask); Flatgen leaves that question alone, as it leaves the Create World screen.

## Tests

- GameTest: the dimensions' chunks have the stacks' blocks.
- Dev client: through a Nether portal and back; into the End and out.

## Tried

NeoForge dev client, a library preset with a Nether of bedrock, three netherrack and soul sand (Soul Sand Valley) and an
End of 49 end stone: Done and Create New World (after the game's experimental-settings question) made a world whose
level.dat has superflat generators of exactly those stacks in the Nether and the End, dimension types unchanged. A
portal lit in the overworld led to the flat Nether, where the game built its partner portal on the soul sand, and back.
An End portal led onto the obsidian platform flush in the end stone; the dragon flew over the flat End, the exit
podium stood at the centre, and once the dragon was killed the portal lit, a gateway appeared, and the exit portal led
through the credits back to the overworld. The world loaded again without a question.
