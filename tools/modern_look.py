"""Writes the sprites and fonts of Flatgen's look (F29): python3 tools/modern_look.py.

Rounded panels, buttons, fields and switches are nine-slice sprites, and the icons are line drawings in the manner of
the design canvas, every one drawn here from numbers. Each is drawn as finely as the sharpest fonts, its edges smoothed
by sampling each texture pixel sixteen times, and written to the game's GUI atlas at textures/gui/sprites/modern/ with
the .mcmeta that tells the game how to stretch it. The fonts are the Inter files Flatgen ships, defined once per text
style and window pixels per GUI pixel (TextStyle). The colours the screens draw text in are in Look.java.
"""
import math
import os
import struct
import zlib

ASSETS = os.path.join(os.path.dirname(__file__), "..", "flatgen", "src", "main", "resources", "assets", "flatgen")
OUT = os.path.join(ASSETS, "textures", "gui", "sprites", "modern")
FONTS = os.path.join(ASSETS, "font")
# Each text style of TextStyle: its Inter file and its size in GUI pixels.
TEXT_STYLES = {"body": ("inter-regular.ttf", 7), "strong": ("inter-semibold.ttf", 7),
               "title": ("inter-semibold.ttf", 10)}
# The most window pixels per GUI pixel a font is defined for (TextStyle.SHARPEST).
SHARPEST = 6
# How many texture pixels a Flatgen GUI pixel gets: as many as window pixels at the sharpest fonts.
DENSITY = SHARPEST
# Samples taken across each texture pixel, each way, to smooth the edges.
SAMPLES = 4

# Each panel: its fill, its border and the border's width, its corner radius, in Flatgen GUI pixels, a third of the
# design canvas's at 1920 by 1080. A fill of None leaves the inside clear.
PANELS = {
    "panel": ("#1d2024", "#2e3238", 0.4, 5),
    "sheet": ("#1a1c20", "#2e3238", 0.4, 8),
    "card_chosen": ("#1c2633", "#5aa9ff", 0.7, 5),
    "button": ("#23262b", "#33373e", 0.4, 4),
    "button_lit": ("#2c3036", "#4a5059", 0.4, 4),
    "button_off": ("#1b1d21", "#2a2d32", 0.4, 4),
    "button_primary": ("#3e8f33", "#3e8f33", 0.4, 4),
    "button_primary_lit": ("#4aa23e", "#4aa23e", 0.4, 4),
    "button_accent": ("#2a3a52", "#3b5a85", 0.4, 4),
    "button_accent_lit": ("#31445f", "#4f74a8", 0.4, 4),
    "button_danger": ("#b83a33", "#b83a33", 0.4, 4),
    "button_danger_lit": ("#cc463e", "#cc463e", 0.4, 4),
    "button_vivid": ("#8c3ce0", "#8c3ce0", 0.4, 5),
    "button_vivid_lit": ("#9d52ea", "#9d52ea", 0.4, 5),
    "field": ("#16181b", "#3b4048", 0.4, 4),
    "field_lit": ("#16181b", "#5aa9ff", 0.7, 4),
    "segments": ("#1a1c20", "#2e3238", 0.4, 5),
    "segment_on": ("#2c3036", "#2c3036", 0.4, 3),
    "switch_on": ("#62c554", "#62c554", 0.4, 5),
    "switch_off": ("#3a3f46", "#3a3f46", 0.4, 5),
    "knob": ("#ffffff", "#ffffff", 0.4, 4),
    "count": ("#0f1012", "#1d2024", 0.7, 5),
    "chip": ("#111214b8", "#111214b8", 0.4, 4),
    "warning": ("#f2b33d", "#f2b33d", 0.4, 4),
    "focus": (None, "#ffffff", 0.7, 5),
    "ring_chosen": (None, "#5aa9ff", 0.7, 5),
    "tooltip": ("#0f1012f2", "#3b4048", 0.4, 4),
}

INK = "#e8eaed"
MUTED = "#9aa0a6"
DIM = "#5f656d"
# Each icon: its colour and its strokes in a 24 unit square: lines (polylines of points, where an ("arc", cx, cy, r,
# from, to) spells out an arc, degrees clockwise on screen from the right), circles (cx, cy, r), dots (cx, cy) and
# rounded rectangles (left, top, width, height, radius), drawn two units wide.
ICONS = {
    "plus": (INK, {"lines": [[(12, 5), (12, 19)], [(5, 12), (19, 12)]]}),
    "minus": (INK, {"lines": [[(5, 12), (19, 12)]]}),
    "cross": (INK, {"lines": [[(18, 6), (6, 18)], [(6, 6), (18, 18)]]}),
    "check": (INK, {"lines": [[(20, 6), (9, 17), (4, 12)]]}),
    "trash": (INK, {"lines": [[(3, 6), (21, 6)], [(8, 6), (8, 4), (16, 4), (16, 6)],
                              [(19, 6), (18, 20), (6, 20), (5, 6)]]}),
    "undo": (INK, {"lines": [[(9, 14), (4, 9), (9, 4)], [(4, 9), (14.5, 9), ("arc", 14.5, 14.5, 5.5, -90, 90),
                                                         (11, 20)]]}),
    "presets": (INK, {"lines": [[(4, 19.5), (4, 4.5), ("arc", 6.5, 4.5, 2.5, 180, 270), (20, 2), (20, 22),
                                 (6.5, 22), ("arc", 6.5, 19.5, 2.5, 90, 270), (20, 17)]]}),
    "templates": (INK, {"rects": [(3, 3, 7, 7, 1), (14, 3, 7, 7, 1), (3, 14, 7, 7, 1), (14, 14, 7, 7, 1)]}),
    "advanced": (INK, {"lines": [[(3, 7), (6, 7)], [(12, 7), (21, 7)], [(3, 17), (12, 17)], [(18, 17), (21, 17)]],
                       "circles": [(9, 7, 3), (15, 17, 3)]}),
    "copy": (INK, {"rects": [(9, 9, 13, 13, 2)],
                   "lines": [[(5, 15), (4, 15), ("arc", 4, 13, 2, 90, 180), (2, 4), ("arc", 4, 4, 2, 180, 270),
                              (13, 2), ("arc", 13, 4, 2, 270, 360), (15, 5)]]}),
    "add_layer": (INK, {"lines": [[(3, 13), (21, 13)], [(3, 17), (21, 17)], [(3, 21), (21, 21)],
                                  [(12, 2), (12, 9)], [(8.5, 5.5), (15.5, 5.5)]]}),
    "arrow_left": (INK, {"lines": [[(19, 12), (5, 12)], [(12, 5), (5, 12), (12, 19)]]}),
    "arrow_up": (INK, {"lines": [[(12, 19), (12, 5)], [(5, 12), (12, 5), (19, 12)]]}),
    "external": (MUTED, {"lines": [[(15, 3), (21, 3), (21, 9)], [(10, 14), (21, 3)],
                                   [(18, 13), (18, 19), (16, 21), (5, 21), (3, 19), (3, 8), (5, 6), (11, 6)]]}),
    "more": (INK, {"dots": [(5, 12), (12, 12), (19, 12)]}),
    "grip": (DIM, {"dots": [(9, 6), (15, 6), (9, 12), (15, 12), (9, 18), (15, 18)]}),
    "fold": (DIM, {"lines": [[(2, 12), (6, 8), (10, 16), (14, 8), (18, 16), (22, 12)]]}),
    "no": ("#ff5a5a", {"circles": [(12, 12, 8)], "lines": [[(6.5, 17.5), (17.5, 6.5)]]}),
    "falls": ("#1a1200", {"lines": [[(12, 5), (12, 19)], [(19, 12), (12, 19), (5, 12)]]}),
}
# Icons that are another drawn mirrored, left to right.
MIRRORED = {"redo": "undo"}
# How much coarser than the panels an icon's texture is: its 24 unit square is drawn about 8 GUI pixels wide.
ICON_DOWNSCALE = 3
# The width of an icon's strokes and a dot's radius, in its 24 unit square.
STROKE = 2.0
DOT = 1.6


def rgba(hex_color):
    """A colour written #rrggbb, or #rrggbbaa when it lets what is behind show through."""
    digits = hex_color.lstrip("#")
    value = int(digits[:6], 16)
    alpha = int(digits[6:], 16) if len(digits) == 8 else 255
    return (value >> 16 & 255, value >> 8 & 255, value & 255, alpha)


def expand(points):
    """A polyline's points with its arcs spelled out, a point each ten degrees."""
    out = []
    for point in points:
        if point[0] != "arc":
            out.append(point)
            continue
        _, cx, cy, r, start, end = point
        steps = max(4, int(abs(end - start) / 10))
        out.extend((cx + r * math.cos(math.radians(start + (end - start) * i / steps)),
                    cy + r * math.sin(math.radians(start + (end - start) * i / steps))) for i in range(steps + 1))
    return out


def segment_distance(px, py, ax, ay, bx, by):
    """How far a point is from the straight piece between two others."""
    dx, dy = bx - ax, by - ay
    length = dx * dx + dy * dy
    t = 0 if length == 0 else max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / length))
    return math.hypot(px - (ax + t * dx), py - (ay + t * dy))


def rounded_distance(x, y, left, top, w, h, radius):
    """The signed distance from a point to a rounded rectangle's edge, negative inside."""
    cx, cy = left + w / 2, top + h / 2
    qx, qy = abs(x - cx) - (w / 2 - radius), abs(y - cy) - (h / 2 - radius)
    return math.hypot(max(qx, 0), max(qy, 0)) + min(max(qx, qy), 0) - radius


def coverage(px, py, units_per_pixel, inside):
    """How much of a texture pixel is inside a shape, from 0 to 1, sampling it SAMPLES by SAMPLES times."""
    hits = 0
    for sy in range(SAMPLES):
        for sx in range(SAMPLES):
            hits += inside((px + (sx + .5) / SAMPLES) * units_per_pixel, (py + (sy + .5) / SAMPLES) * units_per_pixel)
    return hits / SAMPLES ** 2


def icon_ink(shapes, mirrored):
    """Whether a point of an icon's 24 unit square is in its ink."""
    segments = []
    for line in shapes.get("lines", []):
        points = expand(line)
        segments.extend(zip(points, points[1:]))

    def inside(x, y):
        x = 24 - x if mirrored else x
        return (any(segment_distance(x, y, a[0], a[1], b[0], b[1]) <= STROKE / 2 for a, b in segments)
                or any(abs(math.hypot(x - cx, y - cy) - r) <= STROKE / 2 for cx, cy, r in shapes.get("circles", []))
                or any(math.hypot(x - cx, y - cy) <= DOT for cx, cy in shapes.get("dots", []))
                or any(abs(rounded_distance(x, y, *rect)) <= STROKE / 2 for rect in shapes.get("rects", [])))
    return inside


def draw_icon(color, shapes, mirrored):
    """An icon's pixels, its colour wherever its ink covers them."""
    size = 24 * DENSITY // ICON_DOWNSCALE
    inside = icon_ink(shapes, mirrored)
    scale = ICON_DOWNSCALE / DENSITY
    return size, [[color[:3] + (round(255 * coverage(px, py, scale, inside)),) for px in range(size)]
                  for py in range(size)]


def draw_panel(fill, border, border_width, radius):
    """A panel as small as its corners allow, its border inside its edge: its size in GUI pixels and its pixels."""
    logical = radius * 2 + 4
    size = logical * DENSITY

    def edge(x, y):
        return rounded_distance(x, y, 0, 0, logical, logical, radius) <= 0

    def inner(x, y):
        return rounded_distance(x, y, 0, 0, logical, logical, radius) <= -border_width
    pixels = [[mix(fill, border, coverage(px, py, 1 / DENSITY, edge), coverage(px, py, 1 / DENSITY, inner))
               for px in range(size)] for py in range(size)]
    return logical, size, pixels


def mix(fill, border, outer, inner):
    """A pixel covered by the panel's outer edge this much, and by its inside this much."""
    edge = max(0.0, outer - inner)
    fill_rgba = rgba(fill) if fill else (0, 0, 0, 0)
    border_rgba = rgba(border)
    alpha = edge * border_rgba[3] + inner * fill_rgba[3]
    if alpha == 0:
        return (0, 0, 0, 0)
    channels = [(edge * border_rgba[i] * border_rgba[3] + inner * fill_rgba[i] * fill_rgba[3]) / alpha
                for i in range(3)]
    return tuple(round(c) for c in channels) + (round(alpha),)


def png(width, height, pixels):
    """Pixels as an RGBA PNG file's bytes."""
    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)
    raw = b"".join(b"\x00" + bytes(v for p in row for v in p) for row in pixels)
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


def write(name, size, pixels, scaling):
    """A sprite and the .mcmeta saying how the game stretches it."""
    with open(os.path.join(OUT, name + ".png"), "wb") as file:
        file.write(png(size, size, pixels))
    with open(os.path.join(OUT, name + ".png.mcmeta"), "w") as file:
        file.write('{"gui": {"scaling": %s}}\n' % scaling)


def write_font(style, file, size, pixels):
    """A text style drawn at this many window pixels per GUI pixel, the game's own font for what Inter lacks."""
    with open(os.path.join(FONTS, "%s_%d.json" % (style, pixels)), "w") as out:
        out.write('{"providers": [\n'
                  '  {"type": "ttf", "file": "flatgen:%s", "size": %d, "oversample": %d, "shift": [0, 0]},\n'
                  '  {"type": "reference", "id": "minecraft:default"}\n]}\n' % (file, size, pixels))


def main():
    os.makedirs(OUT, exist_ok=True)
    for name in os.listdir(OUT):
        os.remove(os.path.join(OUT, name))
    for name, (fill, border, border_width, radius) in sorted(PANELS.items()):
        logical, size, pixels = draw_panel(fill, border, border_width, radius)
        write(name, size, pixels, '{"type": "nine_slice", "width": %d, "height": %d, "border": %d}'
              % (logical, logical, radius))
    icons = {name: (color, shapes, False) for name, (color, shapes) in ICONS.items()}
    icons.update({name: (ICONS[source][0], ICONS[source][1], True) for name, source in MIRRORED.items()})
    for name, (color, shapes, mirrored) in sorted(icons.items()):
        size, pixels = draw_icon(rgba(color), shapes, mirrored)
        write("icon_" + name, size, pixels, '{"type": "stretch"}')
    for style, (file, size) in sorted(TEXT_STYLES.items()):
        for pixels in range(1, SHARPEST + 1):
            write_font(style, file, size, pixels)


if __name__ == "__main__":
    main()
