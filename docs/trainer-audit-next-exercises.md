# Trainer Audit: Next Exercise Catalog Work

Scope: catalog-quality review only for `SeedTrainingContent.kt` and `ExerciseStepImages.kt`.
No app source or asset edits are included here.

## Current Catalog Snapshot

- Seed catalog contains 101 exercises across lower body, back, chest, shoulders, arms, core, cardio, and full-body conditioning.
- Step-image mappings exist for 82 seeded exercise IDs.
- Seeded exercises currently missing step-image mappings:
  - `dead_bug` - foundational core drill already used by starter and beginner templates.
  - `kettlebell_deadlift`, `kettlebell_romanian_deadlift`, `kettlebell_sumo_deadlift`, `kettlebell_goblet_squat`, `kettlebell_box_squat`, `kettlebell_reverse_lunge`, `kettlebell_split_squat`, `kettlebell_step_up`, `kettlebell_bent_over_row`, `one_arm_kettlebell_row`, `kettlebell_floor_press`, `kettlebell_shoulder_press`, `half_kneeling_kettlebell_press`, `kettlebell_halo`, `kettlebell_suitcase_carry`, `kettlebell_farmer_carry`, `kettlebell_rack_carry`, `two_hand_kettlebell_swing` - text catalog is accepted and the first dedicated kettlebell visual set is mapped.

## Kettlebell Catalog Added

The current MVP-safe kettlebell set includes beginner and intermediate patterns that users can follow from Korean text plus mapped step art: deadlift, Romanian deadlift, sumo deadlift, goblet squat, box squat, reverse lunge, split squat, step-up, bent-over row, one-arm row, floor press, shoulder press, half-kneeling press, halo, suitcase carry, farmer carry, rack carry, and two-hand Russian-style swing.

Higher-bar QA note: first-pass dedicated visuals are now present for `dead_bug` and all 18 kettlebell IDs. Keep the QA bar high for future replacements: wrong-side unilateral art, missing kettlebell handles, or misleading swing/hinge mechanics should block image replacement even when a file exists.

Excluded for now: kettlebell snatch, Turkish get-up, juggling/flip variants, American overhead swing, and complex windmill/clean progressions. These are useful later, but they require stricter trainer-supervised copy and image QA than the MVP text catalog should imply.

## Missing Foundational Exercises To Add Next

Prioritize exercises that fill beginner regressions, common gym substitutions, and movement-pattern gaps before adding more specialty isolation work.

| Priority | Exercise to add | Why it matters | Suggested step count |
|---|---|---|---:|
| P1 | Bodyweight squat | Safer baseline before goblet, Smith, hack, and barbell squat; useful when equipment is unavailable. | 4 |
| P1 | Incline pushup | Best beginner regression for users who cannot yet perform floor pushups; clearer than only telling users to use knee pushups. | 3 |
| P1 | Knee pushup | Common beginner push regression and useful for home/bodyweight plans. | 3 |
| P1 | Dumbbell Romanian deadlift | More accessible hinge than barbell or full dumbbell deadlift; bridges glute bridge, RDL, and deadlift patterns. | 4 |
| P1 | Assisted chin-up or neutral-grip assisted pull-up | Complements assisted pullup and gives a biceps-friendly vertical pull option. | 4 |
| P2 | Dumbbell chest-supported row | Beginner-friendly free-weight row that reduces low-back demand. | 3 |
| P2 | Seated dumbbell overhead press | Safer shoulder-press regression for users who arch during standing dumbbell press. | 4 |
| P2 | Cable external rotation | Rotator-cuff / shoulder-prep accessory currently absent from the shoulder catalog. | 3 |
| P2 | Standing calf raise | Common non-machine calf option for home or crowded gyms. | 3 |
| P2 | Suitcase carry | Anti-lateral-flexion carry progression; makes left/right loading explicit. | 4 |
| P3 | Hamstring walkout | Bodyweight hamstring bridge between glute bridge and leg curl/RDL. | 4 |
| P3 | Wall sit | Simple quad endurance regression for users not ready for loaded squats. | 2 |

## Existing Exercises With Form Or Left-Right Ambiguity Risk

These are already seeded and mapped, but the next content pass should make side choice, rep counting, or setup constraints more explicit in copy and imagery.

| Exercise ID | Risk | Recommendation |
|---|---|---|
| `dead_bug` | Seed instructions are strong and now have a mapped 4-step top-down visual set. | Re-audit future replacements for opposite-limb clarity before swapping assets. |
| `barbell_bench_press` | Step mapping added with 5 individual images. It remains a high-safety lift, so app-screen QA should confirm bar path, full bar ends, and both hands stay readable in the dialog. | Keep 5 images and continue chest-line copy emphasis. |
| `conventional_deadlift` | Step mapping added with 5 individual images. Setup/bracing/lowering poses are visually close, but the sequence now avoids source-sheet borders and cropped bar ends. | Keep 5 images unless future art can make the brace/lower phases more distinct. |
| `dumbbell_split_squat` | Final step says to perform the opposite side, but the visual sequence can still read as only one lead leg. | Show a clear side marker or final opposite-side frame; keep the same camera angle for comparison. |
| `walking_lunge` | Good right/left wording exists, but users may confuse "step through" with rushing or alternating before regaining balance. | Keep 4 steps, emphasize one completed right rep before the left rep begins. |
| `dumbbell_step_up` | Alternating right/left wording exists, but the image needs to show full foot on box and controlled same-side descent, not jumping from the floor leg. | Use 4 steps with visible right-foot and left-foot alternation. |
| `cable_glute_kickback` | Unilateral movement without explicit right/left completion wording in seed instructions. | Add or revise copy/images to state same reps on both legs; 3 images can work if side completion is named. |
| `pallof_press` | Anti-rotation exercise depends on cable side; current flow can imply only one stance direction. | Show cable on one side and include "turn around and repeat" in the final step/copy. |
| `cable_woodchop` | Diagonal direction and high-to-low vs low-to-high path can be ambiguous. | Keep 4 images and label start side, diagonal pull, finish side, and controlled return. |
| `side_plank` | Unilateral hold; current label does not guarantee both sides are performed. | Add a final instruction cue to switch sides after the hold. |
| `landmine_press` | Often single-arm or staggered-stance; current seed does not make bilateral vs unilateral execution clear. | Decide one-arm or two-hand MVP version before generating the next batch; 4 images either way. |
| `farmer_carry` | Good as bilateral carry, but current image key fallback historically pointed to shoulder isolation; mapped steps should clearly show pickup, walk path, and safe put-down. | Keep 5 images; avoid cropping out the floor and dumbbell path. |
| `medicine_ball_slam` | Power movement can encourage unsafe spinal flexion while picking up the ball. | Keep 5 images and make the final pickup frame as important as the slam frame. |
| `rowing_machine` | Already has 5 steps, but beginners often reverse the sequence. | Preserve the 5-step leg-body-arm / arm-body-leg order and avoid decorative cardio-only images. |

## Recommended Step Counts For Next Image Batch

Minimum next batch should close mapping gaps for seeded foundational exercises before adding new catalog entries.

| Batch item | Type | Recommended images |
|---|---|---:|
| `dead_bug` | Existing seeded exercise missing mapping | 4 |
| `bodyweight_squat` | New foundational addition | 4 |
| `incline_pushup` | New foundational addition | 3 |
| `knee_pushup` | New foundational addition | 3 |
| `dumbbell_romanian_deadlift` | New foundational addition | 4 |
| `assisted_chinup` or `neutral_grip_assisted_pullup` | New foundational addition | 4 |
| `dumbbell_chest_supported_row` | New foundational addition | 3 |
| `seated_dumbbell_overhead_press` | New foundational addition | 4 |
| `cable_external_rotation` | New shoulder-prep addition | 3 |
| `standing_calf_raise` | New foundational addition | 3 |
| `suitcase_carry` | New carry/core addition | 4 |

Recommended next batch total: 39 images.

The first-pass minimum is now complete for `dead_bug` plus the 18 kettlebell exercises. Future image work should focus on replacements that improve pose precision, not on filling missing IDs.

## Catalog Quality Notes

- The current catalog is broad enough for MVP gym plans, but it leans heavily on machine and intermediate variations. The next additions should improve beginner regressions and no-machine fallbacks.
- Several seed instructions still use generic setup language such as "장비와 몸의 기준점을 맞춘 뒤 호흡을 정리합니다." That is acceptable as placeholder scaffolding, but the next trainer pass should replace it for high-risk lifts and unilateral movements.
- For unilateral exercises, image sets should consistently answer three questions: which side starts, when to switch, and whether reps are counted per side or total.
- For barbell lifts, image sets should show the safety setup, not only the lifting motion.
