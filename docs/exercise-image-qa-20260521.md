# Exercise Image QA - 2026-05-21

## Decision

The exercise-art problem is broader than resource mapping. Static mapping checks found no missing drawable IDs for the current catalog, but visual QA found multiple production-blocking assets:

- wrong exercise image reused for another exercise
- character/style mismatch between generated batches
- source-sheet UI marks baked into the asset, especially numbered dots, small corner dots, and circular guide marks
- cropped or partially hidden bodies/equipment
- inconsistent background/card tone

Until replacement art is generated and manually accepted, P0 assets must not be shown as exercise reference images in the app.

## P0 Quarantine

No exercise IDs remain in the P0 quarantine after replacing the sideways `stair_climber` candidate with a forward-facing stepmill sequence.

## Accepted Replacements

These assets have been regenerated, visually checked, wired into the app, and removed from the quarantine list:

- `back_extension`
- `battle_rope`
- `box_squat` - regenerated as a 4-step box touch squat sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/box-squat-clean-20260521/original`.
- `chest_supported_row`
- `close_grip_pushup` - regenerated as a 4-step narrow-hand push-up sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/close-grip-pushup-clean-20260521/original`.
- `dumbbell_deadlift` - regenerated as a 4-step two-dumbbell hip-hinge sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/dumbbell-deadlift-clean-20260521/original`.
- `dumbbell_floor_press` - regenerated as a 4-step floor press sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/dumbbell-floor-press-clean-20260521/original`.
- `dumbbell_shrug`
- `elliptical` - regenerated as a 3-step elliptical trainer sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/elliptical-clean-20260521/original`.
- `farmer_carry`
- `front_raise` - regenerated as a 4-step forward dumbbell raise sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/front_raise-worker-20260521/original`.
- `hack_squat` - regenerated as a 4-step plate-loaded hack squat sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/hack_squat-worker-20260521/original`.
- `hammer_curl`
- `hanging_knee_raise`
- `hip_abduction_machine`
- `hip_adduction_machine`
- `incline_machine_press` - regenerated as a 3-step incline chest press machine sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/incline_machine_press-worker-20260521/original`.
- `inverted_row`
- `landmine_press` - regenerated as a 4-step anchored diagonal barbell press sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/landmine_press-worker-20260521/original`.
- `lat_pulldown` - regenerated as a 4-step front-of-body pulldown sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/lat-pulldown-clean-20260521/original`.
- `medicine_ball_slam`
- `mountain_climber` - regenerated as a 4-step alternating-knee sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/mountain-climber-clean-20260521/original`.
- `overhead_triceps_extension` - regenerated as a 4-step single-dumbbell overhead extension sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/overhead_triceps_extension-worker-20260521/original`.
- `preacher_curl_machine`
- `prone_y_raise` - regenerated as a 3-step chest-supported Y raise sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/prone_y_raise-worker-20260521/original`.
- `reverse_curl`
- `rope_overhead_triceps` - regenerated as a 4-step high-cable rope overhead extension sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/rope_overhead_triceps-worker-20260521/original`.
- `rowing_machine` - regenerated as a 4-step ergometer sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/rowing-machine-clean-20260521/original`.
- `sled_push` - regenerated as a 4-step weighted sled push sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/sled-push-clean-20260521/original`.
- `smith_machine_squat` - regenerated as a 4-step Smith rack squat sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/smith_machine_squat-worker-20260521/original`.
- `stair_climber` - regenerated as a 3-step forward-facing stepmill sequence on 2026-05-21; source saved under `docs/exercise-art-candidates/stair-climber-forward-clean-20260521/original`.
- `t_bar_row`

Accepted source sheets were kept locally under `docs/exercise-art-candidates/replacements-20260521/original`.
The tracked QA contact sheet is `docs/exercise-image-qa-replacements-20260521.png`.

## Audit Notes

- `hip_abduction_machine` and `hip_adduction_machine` look like leg press variants, not abduction/adduction machines.
- The previous `lat_pulldown` visually showed a behind-the-neck pull. It has been replaced with a 4-step front pulldown set and removed from quarantine.
- The previous `incline_machine_press` contained baked-in QA marks and read too close to shoulder press. It has been replaced with a 3-step incline press machine sequence and removed from quarantine.
- The previous `rowing_machine` looked like a seated cable row. It has been replaced with a clean 4-step ergometer sequence and removed from quarantine.
- The previous `box_squat` reused a squat image without a visible box. It has been replaced with a 4-step box squat sequence and removed from quarantine.
- The previous `close_grip_pushup` reused a plank-like image. It has been replaced with a 4-step narrow-hand push-up sequence and removed from quarantine.
- The previous `mountain_climber` reused a plank-like image. It has been replaced with a 4-step alternating-knee sequence and removed from quarantine.
- `back_extension` and `inverted_row` reused plank-like images before their accepted replacements.
- `battle_rope`, `farmer_carry`, `dumbbell_shrug`, `hammer_curl`, and `reverse_curl` still deserve later style refreshes, but they are no longer P0 hidden assets.
- The previous `front_raise` and `prone_y_raise` reused lateral-raise-like images. Both have been replaced and removed from quarantine.
- The previous `dumbbell_deadlift` reused a squat-like image. It has been replaced with a 4-step two-dumbbell hip-hinge sequence and removed from quarantine.
- The previous `dumbbell_floor_press` looked like a bench press. It has been replaced with a no-bench 4-step floor press sequence and removed from quarantine.
- `chest_supported_row`, `preacher_curl_machine`, and `t_bar_row` look like seated cable row.
- The previous `elliptical` looked like treadmill walking. It has been replaced with a 3-step elliptical trainer sequence and removed from quarantine.
- The first replacement `stair_climber` fixed the treadmill confusion but introduced a sideways/crab-walk posture. It was discarded and replaced with a rear three-quarter forward-facing sequence, then removed from quarantine.
- The previous `sled_push` looked like treadmill walking. It has been replaced with a weighted sled push sequence and removed from quarantine.
- `hanging_knee_raise` is a lying core image, not a hanging movement.
- Several remaining non-quarantined images still have minor baked-in marks and should be regenerated in a later quality pass.

## Replacement Acceptance Criteria

Each replacement asset must pass all of these before being enabled:

- One consistent anime trainer character style: black hair, teal training jacket, dark athletic pants, black/teal shoes.
- No in-image numbers, labels, arrows, circular guide marks, UI dots, watermarks, panel borders, or corner marks.
- Same warm off-white studio background across all steps.
- Full body and required equipment visible with generous padding.
- The equipment must persist naturally across steps; weight stacks, cables, bars, benches, foot plates, ropes, boxes, and handles should not appear/disappear between steps.
- Step count should match the movement, not be forced to 3 or 4.
- The image must be useful as a form reference, not only an exercise identifier.

## Next Pass

No P0 replacement remains. The next quality pass should focus on non-quarantined style polish and any contact-sheet findings, especially older accepted assets whose character/background style differs from the 2026-05-21 anime trainer set.

## Audit Automation

Run `scripts/audit_exercise_images.sh` before accepting or wiring any new exercise art. It validates file size, missing drawable references, unreferenced exercise assets, missing thumbnails, step numbering gaps, and writes a visual contact sheet under `tmp/exercise-image-qa/`.
