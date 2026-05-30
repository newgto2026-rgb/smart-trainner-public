#!/usr/bin/env python3
"""Split accepted kettlebell/dead-bug source sheets into app assets."""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIR = ROOT / "docs/exercise-art-candidates/kettlebell-final-20260521/original"
SPLIT_DIR = ROOT / "docs/exercise-art-candidates/kettlebell-final-20260521/split"
RES_DIR = ROOT / "feature/training/impl/src/main/res/drawable-nodpi"
QA_DIR = ROOT / "tmp/exercise-art-audit"

TARGET_SIZE = (720, 800)
BACKGROUND = (248, 243, 232, 255)

SHEETS: dict[str, int] = {
    "dead_bug": 4,
    "kettlebell_deadlift": 4,
    "kettlebell_romanian_deadlift": 4,
    "kettlebell_sumo_deadlift": 4,
    "kettlebell_goblet_squat": 4,
    "kettlebell_box_squat": 4,
    "kettlebell_reverse_lunge": 4,
    "kettlebell_split_squat": 4,
    "kettlebell_step_up": 4,
    "kettlebell_bent_over_row": 4,
    "one_arm_kettlebell_row": 4,
    "kettlebell_floor_press": 4,
    "kettlebell_shoulder_press": 4,
    "half_kneeling_kettlebell_press": 4,
    "kettlebell_halo": 3,
    "kettlebell_suitcase_carry": 4,
    "kettlebell_farmer_carry": 4,
    "kettlebell_rack_carry": 4,
    "two_hand_kettlebell_swing": 4,
}


def fit_on_background(image: Image.Image) -> Image.Image:
    image = image.convert("RGBA")
    image.thumbnail(TARGET_SIZE, Image.Resampling.LANCZOS)
    output = Image.new("RGBA", TARGET_SIZE, BACKGROUND)
    left = (TARGET_SIZE[0] - image.width) // 2
    top = (TARGET_SIZE[1] - image.height) // 2
    output.alpha_composite(image, (left, top))
    return output


def split_sheet(exercise_id: str, panel_count: int) -> list[Path]:
    source = SOURCE_DIR / f"{exercise_id}_sheet.png"
    if not source.exists():
        raise FileNotFoundError(source)

    SPLIT_DIR.mkdir(parents=True, exist_ok=True)
    RES_DIR.mkdir(parents=True, exist_ok=True)

    output_paths: list[Path] = []
    with Image.open(source) as original:
        image = original.convert("RGBA")
        panel_width = image.width / panel_count
        for index in range(panel_count):
            left = round(index * panel_width)
            right = round((index + 1) * panel_width)
            panel = image.crop((left, 0, right, image.height))
            processed = fit_on_background(panel)
            filename = f"exercise_{exercise_id}_step_{index + 1}.png"
            split_path = SPLIT_DIR / filename
            res_path = RES_DIR / filename
            processed.save(split_path, optimize=True)
            processed.save(res_path, optimize=True)
            output_paths.append(res_path)
    return output_paths


def make_contact_sheet(paths: list[Path]) -> Path:
    QA_DIR.mkdir(parents=True, exist_ok=True)
    thumb_size = (180, 200)
    label_height = 36
    columns = 8
    rows = (len(paths) + columns - 1) // columns
    sheet = Image.new("RGB", (columns * thumb_size[0], rows * (thumb_size[1] + label_height)), (255, 255, 255))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()

    for offset, path in enumerate(paths):
        with Image.open(path) as original:
            thumb = original.convert("RGB")
            thumb.thumbnail(thumb_size, Image.Resampling.LANCZOS)
        x = (offset % columns) * thumb_size[0]
        y = (offset // columns) * (thumb_size[1] + label_height)
        sheet.paste(thumb, (x + (thumb_size[0] - thumb.width) // 2, y))
        label = path.stem.replace("exercise_", "")
        draw.text((x + 4, y + thumb_size[1] + 4), label[-26:], fill=(20, 28, 36), font=font)

    out = QA_DIR / "kettlebell_deadbug_contact_sheet.png"
    sheet.save(out, optimize=True)
    return out


def main() -> None:
    written: list[Path] = []
    for exercise_id, panel_count in SHEETS.items():
        written.extend(split_sheet(exercise_id, panel_count))
    contact = make_contact_sheet(written)
    print(f"Wrote {len(written)} step assets")
    print(contact)


if __name__ == "__main__":
    main()
