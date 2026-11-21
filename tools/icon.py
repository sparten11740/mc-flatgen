"""Writes Flatgen's icon: python3 tools/icon.py.

A slab of flat world seen from a corner, as the game draws a block in the inventory: a grass top, and down its sides a
layer of dirt, of stone and of bedrock, each block's face a 16 by 16 texture of its own drawn here in the game's colours
(not the game's own textures), its edges smoothed by sampling. The icon goes to assets/flatgen/icon.png, which the mod
lists show, and the store page uses.
"""
import math
import os
import random
import pngfile

OUT = os.path.join(os.path.dirname(__file__), "..", "flatgen", "src", "main", "resources", "assets", "flatgen",
                   "icon.png")
# The icon's size in pixels, and the space around the slab, in pixels.
SIZE = 512
MARGIN = 28
# The slab: blocks across each way, and its layers from the top down.
BLOCKS = 4
LAYERS = ("dirt", "stone", "bedrock")
# A texture's size in its own pixels, and the samples taken across each icon pixel, each way.
TEXTURE = 16
SAMPLES = 3
# The light each face gets, as the game lights a block's top and its two sides in the inventory.
LIGHT = {"top": 1.0, "left": 0.8, "right": 0.62}
# Each material's colours, light to dark, in the game's palette, and how often each is picked.
PALETTES = {
    "grass": ((0x91, 0xC5, 0x5A), (0x7F, 0xB2, 0x4C), (0x6C, 0x9E, 0x40), (0x5A, 0x88, 0x35)),
    "dirt": ((0x9B, 0x6E, 0x4D), (0x86, 0x60, 0x43), (0x73, 0x52, 0x39), (0x5E, 0x43, 0x2E)),
    "stone": ((0x8F, 0x8F, 0x8F), (0x7F, 0x7F, 0x7F), (0x70, 0x70, 0x70), (0x62, 0x62, 0x62)),
    "bedrock": ((0x8A, 0x8A, 0x8A), (0x5E, 0x5E, 0x5E), (0x3E, 0x3E, 0x3E), (0x24, 0x24, 0x24)),
}
WEIGHTS = (2, 4, 3, 1)
# How many pixels of grass hang over the top of a grass side, at most.
GRASS_FRINGE = 4


def texture(material, seed):
    """A 16 by 16 texture of a material, the same for the same seed: noise in its palette."""
    rng = random.Random(seed)
    palette = PALETTES[material]
    return [[rng.choices(palette, WEIGHTS)[0] for _ in range(TEXTURE)] for _ in range(TEXTURE)]


def grass_side(seed):
    """The side of the top layer: dirt with grass hanging over its top a few pixels, unevenly."""
    rng = random.Random(seed)
    pixels = texture("dirt", seed)
    grass = texture("grass", seed + 1)
    for x in range(TEXTURE):
        for y in range(rng.randint(1, GRASS_FRINGE)):
            pixels[y][x] = grass[y][x]
    return pixels


def face_at(x, y):
    """
    The face of the slab a point of the icon is on, and where on that face, each way from 0 to 1. A block's edge turned
    45 degrees and tipped 30, as the game draws blocks in the inventory, makes the top half as tall as it is wide and a
    side its height in blocks times the edge times cos 30; the slab is as large as fits inside the margin.
    """
    height = len(LAYERS) + 1
    room = SIZE - 2 * MARGIN
    tall = 0.5 + height * math.cos(math.radians(30)) / (BLOCKS * math.sqrt(2))
    width = min(room, room / tall)
    half = width / 2
    top = width / 4
    side = height * width / (BLOCKS * math.sqrt(2)) * math.cos(math.radians(30))
    left = (SIZE - width) / 2
    up = (SIZE - 2 * top - side) / 2
    corner = (left, up + top)
    middle = (left + half, up + 2 * top)
    faces = (("top", corner, (half, -top), (half, top)),
             ("left", corner, (half, top), (0, side)),
             ("right", middle, (half, -top), (0, side)))
    for name, origin, along, down in faces:
        dx, dy = x - origin[0], y - origin[1]
        det = along[0] * down[1] - along[1] * down[0]
        u = (dx * down[1] - dy * down[0]) / det
        v = (along[0] * dy - along[1] * dx) / det
        if 0 <= u < 1 and 0 <= v < 1:
            return name, u, v
    return None


def colour_at(textures, x, y):
    """The colour of the slab at a point, lit by its face, or None off the slab."""
    found = face_at(x, y)
    if found is None:
        return None
    name, u, v = found
    if name == "top":
        block = (int(u * BLOCKS), int(v * BLOCKS))
        pixels = textures[("grass", block)]
        tx, ty = int(u * BLOCKS % 1 * TEXTURE), int(v * BLOCKS % 1 * TEXTURE)
    else:
        layer = int(v * (len(LAYERS) + 1))
        block = int(u * BLOCKS)
        pixels = textures[(name, layer, block)]
        tx, ty = int(u * BLOCKS % 1 * TEXTURE), int(v * (len(LAYERS) + 1) % 1 * TEXTURE)
    return tuple(channel * LIGHT[name] for channel in pixels[ty][tx])


def textures():
    """Every block face's texture: the top's grass, and each side's layers, grass sides first."""
    found = {}
    for bx in range(BLOCKS):
        for by in range(BLOCKS):
            found[("grass", (bx, by))] = texture("grass", 1000 + bx * BLOCKS + by)
    for side in ("left", "right"):
        for block in range(BLOCKS):
            seed = (0 if side == "left" else 500) + block * 10
            found[(side, 0, block)] = grass_side(seed)
            for layer, material in enumerate(LAYERS, 1):
                found[(side, layer, block)] = texture(material, seed + layer)
    return found


def pixel(found, px, py):
    """One icon pixel, the samples that fall on the slab averaged."""
    samples = [colour_at(found, px + (sx + .5) / SAMPLES, py + (sy + .5) / SAMPLES)
               for sy in range(SAMPLES) for sx in range(SAMPLES)]
    hits = [colour for colour in samples if colour is not None]
    if not hits:
        return (0, 0, 0, 0)
    return tuple(round(sum(hit[i] for hit in hits) / len(hits)) for i in range(3)) + (
        round(255 * len(hits) / SAMPLES ** 2),)


def main():
    found = textures()
    rows = [[pixel(found, px, py) for px in range(SIZE)] for py in range(SIZE)]
    with open(OUT, "wb") as file:
        file.write(pngfile.encode(SIZE, SIZE, rows))


if __name__ == "__main__":
    main()
