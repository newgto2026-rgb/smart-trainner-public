# Exercise Art Quality Gate

Updated: 2026-05-21 KST

This is the acceptance gate for any exercise method image copied into `feature/training/impl/src/main/res/drawable-nodpi`. It is intentionally stricter than the general art direction because these images teach movement. A polished image that teaches the wrong setup, limb side, resistance path, or machine mechanics must be rejected.

## Scope

- Gate applies to every `exercise_*_step_*` PNG or WebP in `drawable-nodpi`.
- Gate also applies to every `exercise_thumbnail_*` PNG or WebP derived from those step files.
- Current catalog contains 101 exercises in `SeedTrainingContent.kt`.
- Current direct step mapping contains 82 exercise image sets in `ExerciseStepImages.kt`; the remaining exercises must still pass thumbnail/representative-image QA.
- Current accepted step assets are all exactly `720 x 800`; this is now a fixed contract unless the app image component is redesigned.
- This document does not approve source sheets, candidates, or rejected images. Only split step files that pass the checks below may enter `drawable-nodpi`.

## Fixed Asset Contract

All newly accepted exercise art must follow this contract exactly:

- Step image size: `720 x 800 px`.
- Thumbnail size: `360 x 400 px`, generated from the accepted step image, not from a separate prompt.
- File format: PNG for all newly generated or regenerated exercise art.
- Canvas background: a single warm cream/off-white studio color, target `#FAF6ED` or visually equivalent.
- Background content: no gym room, wall panels, plants, mirrors, windows, floor seams, horizon lines, vignette frames, card boxes, or visible square panel patches.
- Floor treatment: either no distinct floor plane or a very soft contact shadow only. A visible floor/wall boundary line is a reject.
- Subject safe area: keep the trainer, load-bearing contacts, and all required equipment inside a minimum 48 px inset from every edge. Machine/bar/cardio assets should usually use 64 px or more.
- Scale consistency: within one exercise, each step should keep the same camera angle, subject scale, and equipment scale unless the movement requires a small natural shift.
- Source sheets are allowed during generation, but every split app asset must look like an independent clean illustration on the same `720 x 800` canvas.

## Character Contract

Canonical main coach reference: `docs/exercise-art-candidates/source-sheets/trainer_exercise_sheet.png`.

Two Smart Trainer coach variants are allowed only when they still read as the same main coach family:

- Lean young trainer: younger face, black spiky hair, teal zip training jacket, black joggers, black/teal shoes, coral wristbands.
- Muscular trainer: more muscular build, same black hair direction, teal/black training outfit family, black/teal shoes, coral wristbands or compatible accents.

Rules:

- A single exercise sequence must use one character variant only. Do not mix lean and muscular variants within the same exercise.
- Character outfit colors must stay in the teal/black Smart Trainer palette.
- The character may alternate by exercise, but not by step.
- The character must be drawn in the same anime illustration style as the accepted Smart Trainer assets, not photorealistic, 3D toy-like, chibi, or unrelated manga style.
- The main coach identity wins over broad style similarity. If the character does not clearly match the canonical main coach's face family, black spiky hair, teal/black outfit, black/teal shoes, and coral wristbands, replace the source before app export.
- If the trainer identity or variant differs from the accepted Smart Trainer character set, replace the image source. Do not accept it by cropping, recoloring, or background cleanup.
- If one step in a sequence uses a different character from the other steps, replace that step/source before app export; a mixed-character sequence cannot pass QA.

## Hard Rejects

Reject or remove from mapping if any item is true:

- Embedded labels, numbers, arrows, captions, logos, watermarks, panel markers, or crop handles remain in the image.
- A source-sheet divider, neighboring panel edge, vertical boundary line, corner dot, tiny badge, or stray mark is visible at app thumbnail or dialog-step size.
- Head, hands, feet, load-bearing contact points, handles, plates, bench/rack ends, box surface, pedal, cable attachment, or machine stack is cropped.
- The trainer identity changes away from the canonical Smart Trainer anime coach: black spiky hair, teal jacket, black joggers, black/teal shoes, coral wristbands, lean-athletic build.
- A step uses a different trainer character than the other steps in the same exercise sequence.
- The image has duplicate limbs, missing limbs, ghost joints, impossible hand/foot attachment, or a limb that cannot be anatomically traced to the torso.
- The movement phase contradicts the written instruction or could teach an unsafe pattern.
- Two or more visual steps are near-duplicates unless one is an intentional static hold and the text explains that the image is repeated for timing only.

## File And Export Checks

Before copying into `drawable-nodpi`:

- Use file names exactly as `exercise_<exercise_id>_step_<n>.<png|webp>`, with contiguous step numbers starting at 1.
- Export each step at `720 x 800`; do not rely on runtime scaling to hide inconsistent crops.
- Keep the subject centered inside a safe area of roughly 48 px from all edges, with larger padding for machines, bars, boxes, and cardio equipment.
- Prefer PNG for newly accepted generated art when preserving crisp illustration edges matters. WebP is acceptable only if app-scale review shows no compression blur, muddy lines, or color shifts.
- Keep a candidate sheet or QA note outside app resources when useful, but do not ship sheet artifacts in app resources.

## Crop And Scale Gate

Every accepted step must pass two views:

- Thumbnail card view: the exercise must still be recognizable at list-row size.
- Detail-step view: body, equipment, and motion path must be understandable without zooming.

Reject if:

- The trainer is tiny with excessive cream background, or oversized with clipped shoes, hands, equipment edges, or bar ends.
- Subject scale jumps between steps in the same exercise.
- A machine or rack is readable in one step but cropped or simplified away in another.
- The crop includes source-sheet gutters, rounded sheet borders, step numbers, tiny icon strings, or neighbor-panel bleed.

## Machine Consistency Gate

Machine exercises have the highest failure risk. Accept only if the same machine reads continuously across every step.

Required:

- Seat, back pad, frame, base feet, handles/pads, guide rods, pulley housing, cable/lever route, and weight stack stay in the same location and perspective.
- Resting phases show plates settled naturally on the stack.
- Active pull/press/curl phases show selected plates lifted with a small visible gap, or another plausible resistance mechanism.
- The handle, pad, cable, lever, or linkage visibly connects the trainer's movement to the lifted stack.
- Assisted pull-up and assisted dip art must keep overhead handles, dip handles, foot step, moving knee pad, frame, cable/linkage, and stack present in every applicable panel.

Reject if:

- The machine becomes a different model between panels.
- The stack does not move during an active phase, moves in the wrong direction, floats, duplicates, or disconnects from the cable/lever.
- A handle or pad changes attachment point in a way that changes the exercise.
- A one-shot generated multi-panel image cannot keep fixed equipment stable enough. In that case, reduce the visual sequence or use a locked-base/layered workflow.

## Left/Right Limb Gate

Side-specific movements need explicit proof, not vibes.

Required:

- Written instruction side and image side must match. If text says right foot, right arm, left leg, or opposite side, the image must make that side readable.
- Alternating movements must show an unambiguous side change, or use one-side visuals plus text that clearly says to repeat on the other side.
- Opposite-limb patterns must show opposite arm and leg, not same-side extension.
- If side is ambiguous from a side view, regenerate with a slight top-down three-quarter angle or reduce the image sequence.

Reject if:

- Walking lunge, step-up, split-squat, bird dog, or dead bug panels appear to repeat only one side while text implies alternation.
- Dead bug shows same-side arm/leg extension, feet planted, bicycle-crunch form, crunch form, leg-raise form, or low-back arch.
- The intended lead foot, support foot, working arm, or reaching limb is hidden by crop or equipment.

## Panel Artifact Gate

Generated sheets often leave artifacts after splitting. These are hard rejects in app resources:

- Visible vertical divider lines or gutters.
- Rounded source-sheet border fragments.
- Step badges, tiny numbered dots, corner UI-like marks, or decorative mini-icons.
- Half-visible neighboring panels.
- Mismatched panel background gradients caused by uneven crops.
- Manual correction marks, circles, arrows, or highlights.

Do not accept a step because the artifact is small. If it is visible in a phone screenshot, it is visible enough to fail.

## Step Count Gate

Step count follows instructional value, not a forced template.

Use fewer image steps plus text when:

- The generated middle or return phase changes machine geometry.
- The only difference between two panels is a tiny limb angle shift.
- A second-side panel creates left/right ambiguity.
- A static hold or tempo cue is better communicated by text.
- A return phase is the same path as the working phase and can be described safely.

Acceptable reductions:

- Two images: setup and peak/working position, with text explaining controlled return.
- Three images: setup, work phase, finish/return when each is visually distinct.
- One image: static setup or machine configuration, with text covering tempo and range, only when a multi-step sequence would be misleading.

Preferred examples:

- `bird_dog`: one side can be visualized; opposite-side repetition belongs in text if the second-side image is ambiguous.
- `machine_shoulder_press`: use fewer panels or locked-base layers if generated panels cannot keep the same machine.
- `dead_bug`: the previous paused set has been replaced by a top-down first-pass visual set. Future replacements still require manual opposite-limb QA; text-only is safer than wrong left/right art.

## Minimum QA Evidence

For each accepted exercise image set, record:

- Exercise id and step count.
- Candidate/source path or generation note.
- Confirmation that no embedded text, numbers, logos, watermarks, sheet borders, panel dividers, or correction marks remain.
- Anatomy check: head, torso, arms, hands, legs, feet, and load-bearing contacts.
- Crop/scale check at `720 x 800`, list thumbnail, and detail-step size.
- Machine consistency check for any machine, cable, rack, bench, box, cardio, or pulley equipment.
- Left/right check for unilateral, alternating, or opposite-limb movements.
- Any accepted caveat and why it is not safety-critical.

## Current Highest-Risk Accepted Assets

Based on current filenames, accepted-art notes, rejected queues, and available screenshots/assets, these should be rechecked before final PR acceptance:

- Previous `exercise_dead_bug_step_1.webp` through `exercise_dead_bug_step_4.webp`: rejected high-risk sources. Current shipped files are PNG replacements generated in the 2026-05-21 top-down pass and mapped only after contact-sheet QA.
- `exercise_machine_shoulder_press_step_1.png` through `exercise_machine_shoulder_press_step_3.png`: medium-high risk. Current screenshots show acceptable app-scale readability, but prior generated versions failed machine continuity and fixed-base notes warn that non-layered generation cannot guarantee identical equipment.
- `exercise_conventional_deadlift_step_1.png` through `exercise_conventional_deadlift_step_5.png`: medium risk. The accepted 2026-05-21 individual PNGs remove panel residue and bar-end crops, and emulator dialog QA on 2026-05-21 confirmed all five steps render with readable bar, hands, and feet. Setup/bracing/lowering phases remain visually close but are paired with distinct copy.
- `exercise_barbell_bench_press_step_1.png` through `exercise_barbell_bench_press_step_5.png`: medium risk. The accepted 2026-05-21 individual PNGs preserve both hands and full bar ends, and emulator dialog QA on 2026-05-21 confirmed all five steps render without panel artifacts. Keep chest-line copy emphasis because the lower phases can still be misread if shown without text.
- `exercise_cable_lateral_raise_step_1.png` through `exercise_cable_lateral_raise_step_3.png`: medium-high risk. Existing notes flag character/style drift toward a sharper, more muscular look.
- `exercise_barbell_bent_over_row_step_1.png` through `exercise_barbell_bent_over_row_step_4.png`: medium risk. Accepted only for catalog use in notes; final app-screen QA was still needed.
- `exercise_romanian_deadlift_step_1.png` through `exercise_romanian_deadlift_step_5.png`: medium risk. Existing notes flag the deepest hinge as more knee-bent than ideal, and screenshots show visible panel residue in thumbnails.
- `exercise_leg_press_step_1.png` through `exercise_leg_press_step_3.png`: medium risk. Accepted with a minor machine-edge crop in the press phase.
- `exercise_one_arm_dumbbell_row_step_1.png` through `exercise_one_arm_dumbbell_row_step_4.png`: medium risk. Accepted with support-side lower leg/foot partly hidden in some panels.
- `exercise_arnold_press_step_1.png` through `exercise_arnold_press_step_4.png`: medium risk. Accepted with final return close to the start pose, so the extra step may add little instructional value.
- `exercise_cable_crunch_step_1.png` through `exercise_cable_crunch_step_3.png`: medium risk. Accepted with feet tucked or partly hidden by the kneeling pose.
- `exercise_walking_lunge_step_1.png` through `exercise_walking_lunge_step_4.png`, `exercise_dumbbell_step_up_step_1.png` through `exercise_dumbbell_step_up_step_2.png`, and `exercise_dumbbell_split_squat_step_1.png` through `exercise_dumbbell_split_squat_step_4.png`: medium risk. These are side-dependent and had earlier rejected same-side or bad-split candidates; `dumbbell_step_up` intentionally ships only the setup and top-position visuals while opposite-side repetition is handled in text.
- `exercise_leg_curl_step_*`, `exercise_hack_squat_step_*`, `exercise_smith_machine_squat_step_*`, and nearby lower-body machine thumbnails: medium risk from screenshots because several thumbnails show visible sheet residue, small step badges, or inconsistent crop/scale. `hip_thrust` was reopened and replaced with `clean_v7` PNG assets on 2026-05-23.
- 107 remaining WebP step assets: medium-high risk as a group. The 2026-05-21 consistency audit found many old-style WebP steps with source-sheet UI, step badges, rounded borders, or colored review marks. Treat these as regeneration debt, not as complete final art.

Assets in this list are not automatically rejected. They need explicit QA evidence against this gate before being treated as final.
