#!/usr/bin/env python3
"""Generate clean list thumbnails from accepted exercise step art.

The step images are intentionally preserved for the detail dialog. These
thumbnails remove source-sheet edges and most tiny step badges before the art is
shown in dense list rows.
"""

from __future__ import annotations

import re
from collections import defaultdict
from pathlib import Path

from PIL import Image, ImageDraw, ImageStat


ROOT = Path(__file__).resolve().parents[1]
RES_DIR = ROOT / "feature/training/impl/src/main/res/drawable-nodpi"
OUTPUT_SIZE = (360, 400)
STEP_RE = re.compile(r"exercise_(?P<id>.+)_step_(?P<step>\d+)\.(?:png|webp)$")

OVERHEAD_OR_TALL_IDS = {
    "arnold_press",
    "assisted_dip",
    "assisted_pullup",
    "barbell_overhead_press",
    "cable_lateral_raise",
    "dumbbell_lateral_raise",
    "dumbbell_shoulder_press",
    "front_raise",
    "hanging_knee_raise",
    "landmine_press",
    "lat_pulldown",
    "machine_shoulder_press",
    "overhead_triceps_extension",
    "prone_y_raise",
    "rope_overhead_triceps",
    "shoulder_press",
    "straight_arm_pulldown",
}

WIDE_MACHINE_IDS = {
    "elliptical",
    "indoor_bike",
    "leg_press",
    "rowing_machine",
    "seated_cable_row",
    "sled_push",
    "stair_climber",
    "treadmill_walk",
}

CUSTOM_CROPS = {
    # The source panel has a left divider close to the body; shift right for the
    # dense thumbnail while keeping the full hinge visible.
    "romanian_deadlift": (160, 80, 700, 680),
}


def choose_representative(steps: dict[int, Path]) -> Path:
    numbers = sorted(steps)
    if not numbers:
        raise ValueError("empty step map")
    if len(numbers) >= 4 and 3 in steps:
        return steps[3]
    if 2 in steps:
        return steps[2]
    return steps[numbers[0]]


def crop_box(exercise_id: str, image: Image.Image) -> tuple[int, int, int, int]:
    if exercise_id in CUSTOM_CROPS:
        return CUSTOM_CROPS[exercise_id]

    width, height = image.size
    target_ratio = OUTPUT_SIZE[0] / OUTPUT_SIZE[1]

    if exercise_id in OVERHEAD_OR_TALL_IDS:
        top = int(height * 0.035)
        crop_height = int(height * 0.86)
        crop_width = int(crop_height * target_ratio)
        left = max(0, int((width - crop_width) * 0.52))
    elif exercise_id in WIDE_MACHINE_IDS:
        top = int(height * 0.055)
        crop_height = int(height * 0.86)
        crop_width = int(crop_height * target_ratio)
        left = max(0, int((width - crop_width) * 0.02))
    else:
        top = int(height * 0.10)
        crop_height = int(height * 0.76)
        crop_width = int(crop_height * target_ratio)
        left = max(0, int((width - crop_width) * 0.42))

    right = min(width, left + crop_width)
    bottom = min(height, top + crop_height)
    left = max(0, right - crop_width)
    top = max(0, bottom - crop_height)
    return left, top, right, bottom


def sampled_corner_color(image: Image.Image) -> tuple[int, int, int, int]:
    left = max(0, image.width // 2 - 36)
    right = min(image.width, image.width // 2 + 36)
    patch = image.crop((left, 0, right, min(48, image.height)))
    median = ImageStat.Stat(patch).median
    return tuple(int(channel) for channel in median[:3]) + (255,)


def hide_sheet_ui_marks(image: Image.Image, exercise_id: str) -> Image.Image:
    """Cover small source-sheet badges in corners without touching step art."""

    cleaned = image.copy()
    draw = ImageDraw.Draw(cleaned, "RGBA")
    corner = sampled_corner_color(cleaned)
    soft = (*corner[:3], 236)
    # Top-left numbered dots and duplicated mini badges.
    draw.rectangle((0, 0, 110, 82), fill=soft)
    # Tiny decorative node marks generated in the top-right of some sheets.
    draw.rounded_rectangle((cleaned.width - 64, 0, cleaned.width, 50), radius=10, fill=soft)
    # Thin source-sheet border fragments on the bottom edge.
    draw.rectangle((0, cleaned.height - 10, cleaned.width, cleaned.height), fill=corner)
    return cleaned


def generate() -> int:
    grouped: dict[str, dict[int, Path]] = defaultdict(dict)
    for path in RES_DIR.glob("exercise_*_step_*.*"):
        match = STEP_RE.fullmatch(path.name)
        if not match:
            continue
        grouped[match.group("id")][int(match.group("step"))] = path

    for exercise_id, steps in sorted(grouped.items()):
        source = choose_representative(steps)
        with Image.open(source) as original:
            image = original.convert("RGBA")
            cropped = image.crop(crop_box(exercise_id, image))
            resized = cropped.resize(OUTPUT_SIZE, Image.Resampling.LANCZOS)
            cleaned = hide_sheet_ui_marks(resized, exercise_id)
            out = RES_DIR / f"exercise_thumbnail_{exercise_id}.png"
            cleaned.save(out, optimize=True)

    return len(grouped)


if __name__ == "__main__":
    count = generate()
    print(f"Generated {count} exercise thumbnails in {RES_DIR}")
