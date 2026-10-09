"""Uniform grid processing. Successful decoding does not establish scene meaning."""
from __future__ import annotations
from io import BytesIO

from PIL import Image, UnidentifiedImageError

MIN_PANEL_SIDE = 32
MAX_PIXELS = 20_000_000
MAX_BYTES = 10 * 1024 * 1024


class InvalidStoryboardError(ValueError):
    """The supplied image is not a usable, uniform PNG storyboard."""


def _decode(grid_png: bytes) -> Image.Image:
    if not isinstance(grid_png, bytes) or not grid_png or len(grid_png) > MAX_BYTES:
        raise InvalidStoryboardError("Expected PNG bytes up to 10 MiB")
    try:
        with Image.open(BytesIO(grid_png), formats=["PNG"]) as image:
            width, height = image.size
            if (width < 3 * MIN_PANEL_SIDE or height < 3 * MIN_PANEL_SIDE
                    or width % 3 or height % 3 or width * height > MAX_PIXELS):
                raise InvalidStoryboardError(
                    "Grid sides must be multiples of 3, at least 96 pixels, and at most 20MP")
            if getattr(image, "n_frames", 1) != 1:
                raise InvalidStoryboardError("Animated PNG is not a storyboard")
            image.verify()
        with Image.open(BytesIO(grid_png), formats=["PNG"]) as image:
            image.load()
            return image.convert("RGBA" if "A" in image.getbands() or "transparency" in image.info else "RGB")
    except (OSError, SyntaxError, ValueError, UnidentifiedImageError,
            Image.DecompressionBombError) as exc:
        raise InvalidStoryboardError("Invalid or unsupported PNG storyboard") from exc


def split_storyboard(grid_png: bytes) -> list[bytes]:
    """Return nine equal PNG panels, left to right then top to bottom.

    Non-divisible dimensions are rejected rather than silently dropping pixels.
    """
    with _decode(grid_png) as image:
        width, height = image.width // 3, image.height // 3
        panels = []
        for row in range(3):
            for col in range(3):
                with image.crop((col * width, row * height,
                                 (col + 1) * width, (row + 1) * height)) as panel:
                    output = BytesIO()
                    panel.save(output, format="PNG")
                    panels.append(output.getvalue())
        return panels
