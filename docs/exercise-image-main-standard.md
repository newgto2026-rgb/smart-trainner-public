# Exercise Image Main Standard

Updated: 2026-05-22 KST

This is the short, non-negotiable standard for Smart Trainner exercise images. If the conversation is compacted, resume from this file before generating, accepting, or wiring any exercise image.

## One-Exercise-At-A-Time Workflow

Do not audit or accept exercise images in scattered batches.

The main loop for every exercise is exactly:

1. Check the quality of the existing app image.
2. If there is a problem, fix or regenerate it.
3. Share only the clearly processed, final app-inserted image with the user.

Do not skip step 1. Do not share anything from step 2. Step 3 happens only after the image has been exported into app resources and reviewed at original size.

Tool or image-generation failures, including `bad request` responses, are rejected attempts only. Record the failure if useful, adjust the prompt/export path, and continue the same exercise loop instead of stopping the audit.

After a final replacement has been exported into app resources and checked against this standard, record the status as `Pass`. Do not use ambiguous review-handoff states for inserted app assets. If the user reports any visible defect later, immediately reopen only that exercise as `Needs regeneration`, `Needs crop/export fix`, or `Needs copy fix`.

## Existing Quality Check

Before deciding whether an existing image can stay, explicitly check each item below. A vague "looks fine" review is not acceptable.

- Character identity first: before doing any crop, cleanup, background removal, or app export, confirm the character clearly belongs to the main Smart Trainer coach family. If the character differs from the canonical main coach, stop reviewing that source and regenerate from a new source. Do not attempt to rescue it with crop, recolor, background cleanup, or local patching.
- Lines and borders: no source-sheet divider line, card outline, rounded rectangle edge, crop boundary, neighboring panel sliver, or square background patch is visible.
- Floor/background boundary: no visible floor seam, wall/floor horizon, wood/rubber floor plane, gym-room background, or rectangular background tile remains.
- Background color consistency: every pixel that reads as background must belong to one clean canvas color family. Reject inset patches, local tint changes, panel-colored rectangles, or mixed cream/white regions even when no hard border is visible.
- Center-frame floor residue: do not only inspect the outer edges. The middle of the image must also be free of wood-floor strips, mat-like base patches, horizontal ground bands, source-room shadows, or leftover background blocks behind/under the figure.
- Numbers and marks: no background/source-sheet step number, badge, dot, arrow, label, UI marker, guide circle, watermark, or correction mark remains. Natural numbers or labels that belong on the equipment itself, such as plate weight markings or machine labels, are allowed when they do not read like UI annotations.
- Movement clarity: the exercise phase matches the written step. Setup and return may use the same or very similar image when the real exercise naturally returns to the starting position, but the label/instruction must clearly say it is the return phase. The problem is not image repetition by itself; the problem is unclear or physically wrong motion.
- Physical equipment logic: machines, cables, levers, footplates, sleds, weight stacks, bars, plates, benches, boxes, pedals, and handles must stay connected and move in the physically correct direction.
- Machine continuity: the machine cannot become a different model between steps, and important parts cannot appear/disappear.
- Crop safety: no head, hands, feet, bar ends, handles, pedals, footplates, bench/box edges, weight stacks, plates, or machine base is cut off.
- App-frame readability: the image must look correct in the actual `720 x 800` step frame and `360 x 400` thumbnail frame without hidden crop tricks.

If any item fails, the exercise status is `Needs regeneration`, `Needs crop/export fix`, or `Needs copy fix`; never mark it `Pass`.

For the 101-exercise catalog, work one exercise at a time in catalog order:

1. Open every step image at original size.
2. Open the thumbnail at original size.
3. Compare the images with the Korean/English exercise steps.
4. If the image fails, fix or regenerate that exercise before moving on.
5. If a new source image passes, delete the existing app step/thumbnail files for that exercise ID before exporting the replacement.
6. Export the replacement only from the accepted new source image; do not read, crop, reuse, or patch the previous app resource.
7. Open the newly exported step images and thumbnail at original size.
8. Record the result in `docs/exercise-image-full-audit-20260521.md`.

Contact sheets are allowed only as navigation aids. They are not enough for acceptance.

## User Sharing Rule

Only share final app-inserted images with the user.

- Do not show raw generated candidates.
- Do not show source sheets.
- Do not show intermediate crops, masks, contact sheets, or partially processed files.
- Share only files already exported into `feature/training/impl/src/main/res/drawable-nodpi`.
- If a generated candidate looks promising but still needs processing, inspect it privately, process it, insert it into app resources, then share the final inserted PNGs only.
- If the final inserted PNG fails visual review, do not share it as accepted; mark it as failed and regenerate.

## Replacement Rule

When an exercise is regenerated:

- Target only that exercise ID.
- Delete `exercise_<exercise_id>_step_*` and `exercise_thumbnail_<exercise_id>` from app resources first.
- Do not reuse the old resource filenames after a visual fix. Use a new versioned or clean filename such as `exercise_<exercise_id>_clean_step_<n>.png` and `exercise_thumbnail_<exercise_id>_clean.png`.
- Update `ExerciseStepImages.kt` so the app points only to the new filenames.
- Remove the old app resource files after remapping so stale cached images cannot be mistaken for the latest work.
- Copy the accepted source sheet into `docs/exercise-art-candidates/<exercise-id>-clean-<date>/original/`.
- Extract a foreground-only image first: character plus equipment only, with the source background removed.
- Create a fresh empty `720 x 800` cream canvas for every step.
- Paste the foreground-only character/equipment into that fresh canvas with safe padding.
- Recreate fresh `720 x 800` step PNGs and a fresh `360 x 400` thumbnail from that clean canvas.
- Verify with `sips -g pixelWidth -g pixelHeight` and original-size visual inspection.
- Never mix old and new step files in one exercise sequence.

## Required Sizes

- Detail step image: exactly `720 x 800 px`.
- Thumbnail image: exactly `360 x 400 px`.
- Newly generated art must be PNG.
- Thumbnail must be generated from an accepted step image, not from a separate image prompt.

## Visible App Frame Standard

The image must be composed for the app-visible frame, not merely resized into the right pixel dimensions.

- Generate and export each step as a self-contained near-square portrait composition that reads well inside a `720 x 800` frame.
- The visible subject/equipment should occupy roughly 70-86% of the frame width and 58-78% of the frame height.
- Do not use extremely wide multi-panel crops that make the subject tiny after fitting into `720 x 800`.
- Do not use extremely tall/narrow source crops that leave large empty side gutters.
- Avoid panoramic sheets for machines. If a machine is wide, generate individual step images or a sheet where each panel is already close to `720 x 800`.
- The accepted step image must look correct when shown by itself at original size and when center-cropped/scaled in the app card.
- Reject any image that only looks okay because of manual zooming, aggressive crop, or hidden overflow.
- For source sheets, each panel must already be designed as a `720 x 800`-friendly panel. Splitting should not require guessing, stretching, or cropping off motion-critical parts.

## Required Background

All accepted exercise art must use one clean studio background:

- Warm cream/off-white canvas, target `#FAF6ED` or visually equivalent.
- The full canvas must be one consistent background color family. Do not accept images where the foreground was pasted with a visible different-color source rectangle, even if the rectangle is subtle.
- No gym room.
- No wall panels.
- No plants, mirrors, windows, ceiling lights, or decorative objects.
- No wood floor, rubber floor, treadmill-like floor, or visible floor/wall boundary.
- No square background patch inside the `720 x 800` canvas.
- No card border, rounded rectangle, divider line, panel gutter, or source-sheet edge.
- Only a very soft contact shadow under the body/equipment is allowed.

## Character Standard

The canonical reference for the main Smart Trainer coach is `docs/exercise-art-candidates/source-sheets/trainer_exercise_sheet.png`.

Two Smart Trainer coach variants are allowed only when they still read as the same main coach family:

- Lean young trainer: younger face, black spiky hair, teal zip training jacket, black joggers, black/teal shoes, coral wristbands.
- Muscular trainer: stronger build, same black-hair/teal-black Smart Trainer outfit family, black/teal shoes, compatible coral accents.

Rules:

- One exercise sequence must use one character variant only.
- Do not mix lean and muscular variants inside the same exercise.
- Character style must be clean anime illustration, not photorealistic, chibi, toy-like, 3D render, or unrelated manga style.
- The main coach identity wins over broad style similarity. If the character does not clearly match the canonical main coach's face family, hair, teal/black outfit, shoes, and coral wristbands, reject and regenerate.
- If the character is different from the main Smart Trainer coach family, the image must be regenerated from a new source. Background cleanup, cropping, recoloring, or local patching cannot make a different character acceptable.
- This is a hard pre-export rule: a different character must never be converted into an app resource, even temporarily, and must never be shared as a final inserted image.
- If the character face, body type, outfit, or illustration style does not match one of the two approved Smart Trainer coach variants, do not attempt to save the image by cropping or background edits. Replace the image source for that exercise and mark the exercise `Needs regeneration` until a matching source exists.
- If a step uses a different character than the other steps in the same exercise, the sequence fails. Do not crop, recolor, or partially patch the odd step; replace that step/source so the whole sequence uses one approved character variant.
- If both character style and background color/panel treatment are inconsistent, the whole image sequence is rejected. Do not create a `clean` export from that source.

## Required Framing

- Full body visible unless a machine necessarily hides a small non-critical part.
- Head, hands, feet, load-bearing contacts, handles, plates, pedals, bench, box, rack ends, bar ends, cable attachments, and machine base must not be cropped.
- Keep at least 48 px safe padding from every edge; use 64 px or more for machines, barbells, cardio equipment, boxes, and long cables.
- Scale, camera angle, and equipment perspective must stay consistent across all steps of one exercise.
- For machine exercises, the movement must be visible, not implied. The footplate, sled, lever arm, cable, stack, or handle must clearly move in the expected direction between steps.
- Return phases may match the setup image when the movement genuinely returns to the starting position. Keep the return step if it helps the user understand the repetition cycle; remove it only when it causes side/machine confusion.

## Hard Rejects

Reject and regenerate or re-export if any of these are visible:

- Step numbers, badges, labels, arrows, UI dots, guide circles, logos, or watermarks.
- Background or source-sheet numbers. Equipment-native markings on plates or machines are not rejected by themselves.
- Background color that visibly differs from the standard cream canvas, especially yellow-tinted panels, white outer margins, or an inset card background.
- Panel dividers, neighboring panel slivers, rounded sheet borders, card outlines, or crop marks.
- Cropped hands, feet, head, bar ends, handles, machine stacks, footplates, pedals, bench/box edges, or rack bases.
- Duplicate limbs, missing limbs, impossible joints, or side-specific movement that contradicts the text.
- Machine shape, handles, cables, stacks, plates, or base changing between steps.
- A movement phase that teaches a different exercise or unsafe form.

## Current Source Of Truth

- Full per-exercise audit tracker: `docs/exercise-image-full-audit-20260521.md`.
- Detailed quality gate: `docs/exercise-art-quality-gate.md`.
- App resources: `feature/training/impl/src/main/res/drawable-nodpi`.
- Step mapping: `feature/training/impl/src/main/java/com/smarttrainner/feature/training/impl/ExerciseStepImages.kt`.
- Catalog copy: `core/data/src/main/java/com/smarttrainner/core/data/SeedTrainingContent.kt`.
