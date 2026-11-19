# F22 Flat Nether and End

Milestone: after v1 · Release: 1.0.0 while it is unreleased · Status: done · Depends on: F20

## Why

Vanilla offers flat Nether and End only from 26.2 ("flat_all_dimensions"), and FlatEdit+ users have asked for it.
On 1.21.1 only datapacks do it today.

## What the player gets

Optional stacks for the Nether and the End in the same editor, each with its own biome.

## Acceptance criteria

- [x] A preset may carry a Nether stack and an End stack (F02's format gains optional `nether` and `end` parts;
  older files load as overworld only).
- [x] Applied by replacing those dimensions' generators with flat ones; the world still opens without Flatgen.
- [x] Portals work: a Nether portal leads to the flat Nether and back; the End has a way back (exit portal and dragon
  handled; first written as Create Aeronautics' "End Sea" does it, by setting the dragon fight data, but the game's
  own fight places the exit portal on the flat ground, so Flatgen leaves its data alone, see Design).
- Moved to F25, which builds the first 26.2 node: on 26.2 and later, vanilla's own `flat_all_dimensions` is offered as
  a built-in.

## Design

- The preset's `nether` and `end` parts (`preset/rules/Stack`: layers and biome) are optional; `Preset.stack(Dimension)`
  gives the overworld's from the preset's own layers and biome, so everything that reads those keeps working.
- Done replaces the Nether's and the End's generators with superflat ones of their stacks (the structures their biome
  allows, as the game's default is, so a flat Nether keeps the fortresses whose blazes give Eyes of Ender; no
  decorations or lakes) and keeps their dimension types, so portals, the dragon fight and the dimensions' sky stay the
  game's; a dimension without a stack gets its world type's generator back, undoing an earlier Done. Reopening Customize
  reads the stacks back from the screen. A datapack export (F21) carries them too.
- The editor (decided by a panel of a user-experience expert and an engineer): three tabs above the column, a grass
  block, a lit Nether portal (Flatgen's own picture) and an Eye of Ender, the chosen one raised as the creative
  inventory's tabs are, choose the dimension the column, the layer, block and biome controls, the summary and the facts
  show and edit. A Nether or End with a stack of its own wears the flat badge, three stripes, on its tab. One without
  shows "Make the Nether flat" (bumpy ground, an arrow, flat stripes), which gives it a first stack: bedrock and 31
  netherrack, the surface at Y 32 where the Nether's lava sea lies, or bedrock and 48 end stone, the top in the layer
  the arrival platform is laid in (`dimensions/rules/NewStacks`). A flat one has "Give back to the game", which asks
  first.
- `EditorState` holds the edited dimension; the edits of layers and biome go through its `withLayers` and `withBiome`,
  so they change whichever stack is edited, and change nothing for a dimension without one. Choosing a tab is no step,
  like selecting a layer; undo goes back to the dimension where the step was made. Each dimension has its own `World`
  (its type's floor and height), so Y labels and limits are the Nether's and the End's own.
- Panels say where they show (`EditorPanel.shows`): by default only for the overworld, as structures, options,
  presets, templates and the superflat code concern the whole preset or the overworld; the stack's own panels show
  for any dimension with a stack, undo and the message for all. The editor lays its widgets out again when the edited
  dimension or whether it has a stack changes.
- Using a template keeps the Nether and the End, as a template says nothing of them; using a saved preset takes its
  own. The game's own editor, opened from Flatgen, edits the overworld only, so Nether and End changes not yet applied
  with Done stay behind in Flatgen.
- The dragon is left to the game: its fight places the exit portal's podium on the flat surface at the centre and
  spawns the dragon, whose death lights the portal, so the End always has a way back without Flatgen touching the
  fight's data.
- The game puts a player arriving in the End on its obsidian platform at Y 48 (X 100, Z 0), so an End stack 49 blocks
  tall from Y 0 meets it flush.
- End cities need End Highlands or Midlands ground at Y 60 or higher, so the End's first stack, flush with the arrival
  platform at Y 48 and in The End's biome, has none; a player after elytra makes the End taller in one of those biomes.
- A data pack that makes the Nether or the End itself wins over the screen's generator in 1.21.1, so for such a
  dimension the editor says a data pack makes it, in its tab's tooltip and in place of "Make flat".
- The game calls a world with a Nether or End not its own "experimental" and asks once, when the world is created
  (loading it again does not ask); Flatgen leaves that question alone, as it leaves the Create World screen.

## Tests

- Unit: the preset file reads what it writes of the Nether and the End, and an older file as the overworld alone
  (`PresetJsonTest`); the stacks kept through every change of the preset (`PresetTest`); edits of the chosen stack
  leaving the overworld alone and a dimension without a stack taking none (`EditorStateTest`); choosing a dimension no
  step (`HistoryTest`); the first stacks and giving and taking back a stack (`DimensionRulesTest`).
- GameTest: a Nether stack generates its layers and biome; a world preset gives the Nether and the End their stacks
  and keeps their types, and without stacks leaves them as the game makes them (`DimensionGameTests`).
- Dev client: through a Nether portal and back; into the End and out.

## Tried

NeoForge dev client, a library preset with a Nether of bedrock, three netherrack and soul sand (Soul Sand Valley) and an
End of 49 end stone: Done and Create New World (after the game's experimental-settings question) made a world whose
level.dat has superflat generators of exactly those stacks in the Nether and the End, dimension types unchanged. A
portal lit in the overworld led to the flat Nether, where the game built its partner portal on the soul sand, and back.
An End portal led onto the obsidian platform flush in the end stone; the dragon flew over the flat End, the exit
podium stood at the centre, and once the dragon was killed the portal lit, a gateway appeared, and the exit portal led
through the credits back to the overworld. The world loaded again without a question.

The editor, NeoForge: the tabs chose the Nether, which showed only "Make the Nether flat" and undo; making it flat gave
bedrock and 31 netherrack, the surface at Y 32 and Y labels from 0, Nether Wastes, and its tab the flat badge; the End
likewise gave a surface at Y 49. Done, then Customize again, read both stacks back from the screen. Give back asked
first, Yes left the End as the game makes it, and Undo brought its stack back. Tab, the arrow keys and Enter chose each
tab. Saving to the library wrote the `nether` and `end` parts. Fabric: the same tabs and first stacks, and Done with
Create New World made a world whose Nether is bedrock and 31 netherrack and whose End is bedrock and end stone. After
the review's fixes, NeoForge: "Make the Nether flat" pressed with Enter left the focus on the panel, so a second Enter
asked to give it back rather than pressing Done, and the Water world template kept the flat Nether.

After the group's review, NeoForge: a world from a preset with a flat Nether had a fortress (`/locate` found it 316
blocks away, built of nether bricks with blaze spawners) and a bastion; with a data pack's own `the_end.json` loaded,
the End tab said a data pack makes it and offered no Make flat. Not tried: the narrator on the tabs.
