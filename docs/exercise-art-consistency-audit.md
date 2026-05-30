# Exercise Art Consistency Audit

Updated: 2026-05-21 KST

## Result

Current exercise art is dimensionally consistent, but not visually complete.

- Step assets: 278 files, all `720 x 800`.
- Thumbnail assets: 83 files, all `360 x 400`.
- Size mismatches: 0.
- Mixed step formats remain: 171 PNG step assets and 107 WebP step assets.
- Background/style outliers: 30 thumbnail assets.

The key issue is not scaling. Several older WebP-derived exercise images contain embedded panel UI, step badges, corner marks, and colored review/correction circles. These cannot be fixed by resizing or converting WebP to PNG because the artifacts are part of the pixels.

Audit evidence:

- CSV: `tmp/exercise-art-audit/asset_audit.csv`
- Summary: `tmp/exercise-art-audit/asset_audit_summary.json`
- Flagged contact sheet: `tmp/exercise-art-audit/flagged_assets_contact_sheet.png`
- New barbell contact sheet: `tmp/exercise-art-audit/new_barbell_assets_contact_sheet.png`

## Accepted Pattern

Accepted production assets should follow this baseline:

- Detail step image: `720 x 800`, preferably PNG, no embedded labels, badges, panel borders, arrows, or review marks.
- List thumbnail: `360 x 400`, generated only from accepted clean step art.
- Background: warm off-white/cream near the current accepted PNG sets, not gray panel sheets or dark UI fragments.
- Character: same Smart Trainer anime coach silhouette, black hair, teal jacket, black joggers, black/teal shoes, coral wristbands.
- Detail posture: image must teach the written step safely enough to be followed without zooming.

## Newly Checked Assets

`barbell_bench_press` and `conventional_deadlift` match the size contract and are visually closer to the accepted direction.

- `barbell_bench_press`: 5 PNG steps and 1 PNG thumbnail, all correct size.
- `conventional_deadlift`: 5 PNG steps and 1 PNG thumbnail, all correct size.
- Emulator dialog QA passed for both exercises on 2026-05-21.

Remaining caveat:

- Bench press lower phases still depend on the copy to reinforce chest-line lowering.
- Deadlift setup/bracing/lowering phases are visually close, but the lockout and controlled lower are readable enough for catalog use.

## Assets That Need Regeneration

The following thumbnails are `360 x 400`, but their background/border tone or visible annotation artifacts make them inconsistent with the newer accepted art:

- `exercise_thumbnail_back_extension.png`
- `exercise_thumbnail_battle_rope.png`
- `exercise_thumbnail_calf_raise.png`
- `exercise_thumbnail_chest_supported_row.png`
- `exercise_thumbnail_close_grip_pushup.png`
- `exercise_thumbnail_dumbbell_curl.png`
- `exercise_thumbnail_dumbbell_floor_press.png`
- `exercise_thumbnail_dumbbell_shrug.png`
- `exercise_thumbnail_dumbbell_step_up.png`
- `exercise_thumbnail_face_pull.png`
- `exercise_thumbnail_farmer_carry.png`
- `exercise_thumbnail_hack_squat.png`
- `exercise_thumbnail_hammer_curl.png`
- `exercise_thumbnail_hanging_knee_raise.png`
- `exercise_thumbnail_hip_abduction_machine.png`
- `exercise_thumbnail_hip_adduction_machine.png`
- `exercise_thumbnail_incline_machine_press.png`
- `exercise_thumbnail_indoor_bike.png`
- `exercise_thumbnail_inverted_row.png`
- `exercise_thumbnail_leg_curl.png`
- `exercise_thumbnail_leg_extension.png`
- `exercise_thumbnail_mountain_climber.png`
- `exercise_thumbnail_pec_deck_fly.png`
- `exercise_thumbnail_plank.png`
- `exercise_thumbnail_preacher_curl_machine.png`
- `exercise_thumbnail_rear_delt_machine.png`
- `exercise_thumbnail_reverse_crunch.png`
- `exercise_thumbnail_reverse_curl.png`
- `exercise_thumbnail_rowing_machine.png`
- `exercise_thumbnail_seated_cable_row.png`
- `exercise_thumbnail_t_bar_row.png`

The 107 WebP step assets should be treated as regeneration debt. Many share the same old sheet style, visible step badges, rounded source-sheet borders, or review marks. Do not mark these complete merely by converting format.

## Decision

Do not call the full exercise image set complete yet.

The app can keep showing already accepted clean PNG assets, including the new barbell work. The remaining WebP-derived or annotated assets need proper regeneration from clean prompts or reference-to-anime transforms before they should be considered final.

## Next Batch Priority

Regenerate in this order:

1. Exercises visible in starter/beginner plans.
2. Exercises with embedded review marks in the center of the motion.
3. Machine exercises where equipment continuity matters.
4. Remaining WebP-only detail step sets.

Every regenerated set must pass:

- File-size contract: `720 x 800` step, `360 x 400` thumbnail.
- No visible annotations, labels, panel borders, badges, or colored correction marks.
- Background and character match the accepted Smart Trainer direction.
- Posture review against the written steps.
- Emulator dialog screenshot review before PR push.
