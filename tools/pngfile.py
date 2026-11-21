"""The PNG files Flatgen's drawing tools write (sprites.py, modern_look.py, icon.py): RGBA, 8 bits a channel."""
import struct
import zlib


def encode(width, height, rows):
    """A PNG of these rows of pixels, top first, each pixel an (r, g, b, a) tuple."""
    raw = b"".join(b"\x00" + bytes(channel for pixel in row for channel in pixel) for row in rows)
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


def chunk(kind, data):
    """One chunk of a PNG: its length, its kind, its data and their checksum."""
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)
