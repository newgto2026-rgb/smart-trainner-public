#!/usr/bin/env python3
"""Split accepted replacement exercise sheets into app assets."""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIR = ROOT / "docs/exercise-art-candidates/replacements-20260521/original"
SPLIT_DIR = ROOT / "docs/exercise-art-candidates/replacements-20260521/split"
RES_DIR = ROOT / "feature/training/impl/src/main/res/drawable-nodpi"
QA_DIR = ROOT / "tmp/exercise-art-audit"

TARGET_SIZE = (720, 800)
THUMBNAIL_SIZE = (360, 400)
BACKGROUND = (248, 243, 232, 255)

SHEETS: dict[str, tuple[int, str]] = {
    "back_extension": (3, "horizontal"),
    "battle_rope": (4, "grid_2x2"),
    "chest_supported_row": (3, "horizontal"),
    "dumbbell_shrug": (3, "horizontal"),
    "farmer_carry": (3, "horizontal"),
    "hammer_curl": (3, "horizontal"),
    "hanging_knee_raise": (3, "horizontal"),
    "hip_abduction_machine": (3, "horizontal"),
    "hip_adduction_machine": (3, "horizontal"),
    "inverted_row": (3, "horizontal"),
    "medicine_ball_slam": (4, "grid_2x2"),
    "preacher_curl_machine": (3, "horizontal"),
    "reverse_curl": (3, "horizontal"),
    "t_bar_row": (3, "horizontal"),
}


def fit_on_background(image: Image.Image, target_size: tuple[int, int]) -> Image.Image:
    image = image.convert("RGBA")
    image.thumbnail(target_size, Image.Resampling.LANCZOS)
    output = Image.new("RGBA", target_size, BACKGROUND)
    left = (target_size[0] - image.width) // 2
    top = (target_size[1] - image.height) // 2
    output.alpha_composite(image, (left, top))
    return output


def crop_panel(image: Image.Image, panel_count: int, layout: str, index: int) -> Image.Image:
    if layout == "horizontal":
        panel_width = image.width / panel_count
        left = round(index * panel_width)
        right = round((index + 1) * panel_width)
        return image.crop((left, 0, right, image.height))

    if layout == "grid_2x2":
        columns = 2
        rows = 2
        panel_width = image.width / columns
        panel_height = image.height / rows
        column = index % columns
        row = index // columns
        left = round(column * panel_width)
        top = round(row * panel_height)
        right = round((column + 1) * panel_width)
        bottom = round((row + 1) * panel_height)
        return image.crop((left, top, right, bottom))

    raise ValueError(f"Unsupported layout: {layout}")


def split_sheet(exercise_id: str, panel_count: int, layout: str) -> list[Path]:
    source = SOURCE_DIR / f"{exercise_id}_sheet.png"
    if not source.exists():
        raise FileNotFoundError(source)

    SPLIT_DIR.mkdir(parents=True, exist_ok=True)
    RES_DIR.mkdir(parents=True, exist_ok=True)

    output_paths: list[Path] = []
    with Image.open(source) as original:
        image = original.convert("RGBA")
        for index in range(panel_count):
            panel = crop_panel(image, panel_count, layout, index)
            processed = fit_on_background(panel, TARGET_SIZE)
            filename = f"exercise_{exercise_id}_step_{index + 1}.webp"
            split_path = SPLIT_DIR / filename
            res_path = RES_DIR / filename
            processed.convert("RGB").save(split_path, quality=96, method=6)
            processed.convert("RGB").save(res_path, quality=96, method=6)
            output_paths.append(res_path)

    thumbnail_source = output_paths[min(1, len(output_paths) - 1)]
    with Image.open(thumbnail_source) as original:
        thumb = fit_on_background(original, THUMBNAIL_SIZE)
        thumb_path = RES_DIR / f"exercise_thumbnail_{exercise_id}.png"
        thumb.save(thumb_path, optimize=True)
        output_paths.append(thumb_path)

    return output_paths


def make_contact_sheet(paths: list[Path]) -> Path:
    QA_DIR.mkdir(parents=True, exist_ok=True)
    thumb_size = (180, 200)
    label_height = 36
    columns = 5
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
        draw.text((x + 4, y + thumb_size[1] + 4), label[-28:], fill=(20, 28, 36), font=font)

    out = QA_DIR / "replacement_assets_contact_sheet.png"
    sheet.save(out, optimize=True)
    return out


def main() -> None:
    written: list[Path] = []
    for exercise_id, (panel_count, layout) in SHEETS.items():
        written.extend(split_sheet(exercise_id, panel_count, layout))
    contact = make_contact_sheet(written)
    print(f"Wrote {len(written)} replacement assets")
    print(contact)


if __name__ == "__main__":
    main()
