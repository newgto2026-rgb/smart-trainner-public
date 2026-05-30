# Smart Trainer Exercise Art Direction

Updated: 2026-05-21 KST

This document is the stable visual contract for exercise method images. Use it whenever new exercise art is generated, reviewed, cropped, or added to the Android resources.

## Character Bible

- Canonical character reference: `feature/training/impl/src/main/res/drawable-nodpi/trainer_exercise_sheet.png`.
- The guide character is the exact anime trainer from that 12-panel reference sheet, not a new person per exercise.
- Young adult male anime trainer with short spiky black hair, clear friendly facial features, athletic but approachable proportions.
- Outfit is fixed from the reference sheet: teal zip training jacket, black jogger pants, black trainers with teal accents, coral wristbands.
- Body style is lean-athletic, not bulky bodybuilder and not toy-like.
- The character should feel modern, precise, energetic, and consistent with the Smart Trainer brand.
- Generated character sheets or alternate character designs are rejected unless the user explicitly approves them.

## Brand Tone

- Palette: bright cream studio background, teal/cyan action accents, deep navy outlines, small coral wristband accents.
- Mood: polished mobile fitness guide, modern and clean, not medical textbook, not noisy gym poster.
- Lighting: soft app illustration lighting with subtle shadows. Avoid dark, muddy, or gray-heavy backgrounds.
- UI compatibility: images must stay readable inside rounded exercise cards and detail dialogs on mobile.

## Exercise Image Rules

- Every exercise uses the same character, outfit, line style, and background tone.
- Every step image must show a distinct phase of the exercise. Do not reuse near-identical poses.
- Step count follows training need, not a forced three-step template.
- Preserve the biomechanical truth of the exercise before style polish.
- If text labels appear in generated art, crop them out or regenerate. App text is rendered by Compose, not embedded in the image.

## Anatomy QA

Before accepting an image, verify:

- One head, one torso, exactly two arms, two hands, two legs, and two feet unless the exercise naturally hides a limb behind equipment.
- Arms connect to shoulders and legs connect to hips without ghost limbs or duplicate joints.
- No cropped head, hands, feet, equipment handles, or load-bearing contact points.
- Equipment path and grip make sense for the exercise.
- The working limb and return limb match the written step.

## Machine Equipment QA

Machine exercise art must show plausible resistance mechanics:

- In setup and return phases, the selected weight plates should rest naturally on the stack.
- In active pull/press/curl phases, the selected upper plates should visibly lift from the remaining stack with a small gap.
- The cable, lever arm, or linkage must connect the handle/pad path to the lifted stack.
- Reject machine art where the user is clearly pressing or pulling but the stack is unchanged, disconnected, floating, duplicated, or moving in the wrong direction.
- Reject machine art where the machine frame, seat, pulley housing, handles, or weight stack changes shape or disappears between panels. The equipment must read as the same machine through setup, work, and return.
- For assisted pull-up and assisted dip stations, the overhead handles, dip handles, foot step, moving knee pad, frame, and weight stack must remain present and spatially consistent in every step. Reject if a foot step or knee pad appears/disappears between panels, if the handles move to a new position, or if the user appears to pull from a different machine.
- Do not overdo motion lines; the plate gap and taut cable should make the load movement readable.

## Dead Bug QA

Trainer-audited dead bug method is four steps. Image models often draw same-side arm and leg extension, so left/right must be explicit.

Status: `dead_bug` image production resumed on 2026-05-21 KST with a top-down visual set to make alternating diagonal limbs easier to audit. Keep future replacements under the same rule: do not ship generated dead bug art unless a manually verified opposite-limb workflow passes this section.

- Internet form references searched on 2026-05-20 KST:
  - Hinge Health: `https://www.hingehealth.com/resources/articles/deadbug/`
  - Verywell Fit: `https://www.verywellfit.com/how-to-do-the-dead-bug-exercise-4685852`
  - Healthline: `https://www.healthline.com/health/exercise-fitness/dead-bug-exercise`
  - GrabGains reviewed exercise guide: `https://grabgains.com/exercises/dead-bug`
- Reference image URLs for model prompting only. Use them for joint/limb geometry, not for identity, clothing, background, or final visual expression:
  - `https://grabgains.com/dist/exercises/thumbnails/02761201.webp`
  - `https://grabgains.com/dist/exercises/thumbnails/027612012.webp`
- Step 1: tabletop start. Back on mat, both arms vertical over shoulders, hips and knees at 90 degrees, shins parallel to floor.
- Step 2: right arm reaches overhead by the ear while left leg extends forward near the floor. Left arm and right leg stay in tabletop.
- Step 3: return to tabletop start position. Both arms vertical, both knees bent 90 degrees.
- Step 4: left arm reaches overhead by the ear while right leg extends forward near the floor. Right arm and left leg stay in tabletop.
- Reject if the exercise looks like crunch, bicycle crunch, bird dog, or simple leg raise.
- Reject if there are duplicated arms, ghost limbs, same-side arm/leg extension, excessive low-back arch, or feet planted on the floor.
- If left/right is ambiguous in side view, regenerate with a slight top-down three-quarter camera angle so both sides are readable.

## Form-Reference Approved App Assets

The following generated assets passed visual QA and were copied into `feature/training/impl/src/main/res/drawable-nodpi` as PNG resources on 2026-05-20 KST:

- `leg_press`: 3 steps, accepted with minor machine-edge crop in the press phase.
- `goblet_squat`: 4 steps, accepted; step 1 and step 4 intentionally show setup and completed return.
- `lat_pulldown`: 3 steps, accepted; grip, vertical cable path, and upper-chest pull phase are readable.
- `seated_cable_row`: 3 steps, accepted; start and return are similar by design, row phase shows handle-to-torso path.
- `machine_chest_press`: 3 steps, accepted; start/return are similar by design, press phase is distinct.
- `machine_shoulder_press`: 3 steps, accepted after user-provided reference rework; the same seated press machine, lever path, handles, guide rails, and weight stack remain coherent through start, press, and overhead finish. Source candidate: `docs/exercise-art-candidates/machine_shoulder_reference_rework/machine_shoulder_press_reference_prompt_candidate.png`; user reference: `docs/exercise-art-references/machine-shoulder-press/user-reference-gemini.png`.
- `leg_extension`: 3 steps, accepted with caveat that the cable/linkage depiction is slightly stylized.
- `leg_curl`: 3 steps, accepted; prone setup, curl phase, and return are readable with stable equipment.
- `calf_raise`: 3 steps, accepted; standing raise phase and lifted stack are readable with stable equipment.
- `dumbbell_bench_press`: 4 steps, accepted after wider-framing regeneration.
- `dumbbell_lateral_raise`: 3 steps, accepted; arms raise sideways to shoulder height.
- `plank`: 2 steps, accepted; forearm support and straight-line hold are readable.
- `side_plank`: 3 steps, accepted; setup, lift, and reach variation keep a straight body line.
- `bird_dog`: 3 visual steps, accepted with the opposite-side repetition handled in text instead of a second-side image to avoid left/right ambiguity.
- `pallof_press`: 4 steps, accepted with caveat that the front-facing view is chosen for readability.
- `cable_woodchop`: 4 steps, accepted; high-to-low cable line and controlled torso rotation are readable.
- `triceps_pushdown`: 3 steps, accepted; stable tower, taut cable, and lifted stack in the pushdown phase.
- `cable_curl`: 3 steps, accepted; rear-facing view is acceptable because low pulley path and curl phase are clear.
- `face_pull`: 3 steps, accepted; rope-to-face path and lifted stack are readable.
- `straight_arm_pulldown`: 3 steps, accepted with minor elbow-softness caveat; still reads as a shoulder pulldown, not a triceps pushdown.
- `barbell_back_squat`: 5 steps, accepted after regenerating with both hands visible on the bar.
- `barbell_bench_press`: 5 steps, accepted from individually generated 720x800 step art after rejecting multi-panel sheets that hid the far arm or cropped bar ends. Caveat: the lower phases should be paired with copy that reinforces chest-line lowering rather than neck/face path.
- `conventional_deadlift`: 5 steps, accepted from individually generated 720x800 step art after rejecting the earlier sheet for rounded panel borders and cropped bar ends. Caveat: setup/bracing/lowering phases are visually close, but the lockout and controlled-lower sequence is readable enough for catalog use.
- `barbell_bent_over_row`: 4 steps, accepted for catalog use; final app-screen QA still needed.
- `barbell_overhead_press`: 4 steps, accepted; both hands remain visible and the bar path is readable.
- `dumbbell_split_squat`: 4 visual steps, accepted only as a single-side demonstration; text explicitly instructs repeating the same reps on the opposite side.
- `walking_lunge`: 4 steps, accepted after alternating-leg rework; setup, right-foot-forward lunge, step-through, and left-foot-forward lunge are visually distinct. Source candidate: `docs/exercise-art-candidates/worker-lower-alternation/walking_lunge_candidate_2_sheet.png`.
- `incline_dumbbell_press`: 4 steps, accepted; bench angle and both dumbbells are readable.
- `dumbbell_shoulder_press`: 4 steps, accepted; both dumbbells remain visible.
- `arnold_press`: 4 steps, accepted with caveat that the final return is close to the start pose.
- `one_arm_dumbbell_row`: 4 steps, accepted with caveat that support-side lower leg/foot is partly hidden in some panels.
- `dumbbell_curl`: 2 steps, accepted; setup and curl positions are clear.
- `assisted_pullup`: 4 steps, accepted after rework; fixed overhead handles, foot step, moving knee pad, frame, cable/linkage, and right-side stack remain present through entry, start, pull, and lower phases. Source candidate: `docs/exercise-art-candidates/worker-pullup-rework/assisted_pullup_sheet.png`.
- `machine_row`: 3 steps, accepted; cable and stack movement are readable.
- `pec_deck_fly`: 3 steps, accepted; frame and stack remain coherent.
- `rear_delt_machine`: 3 steps, accepted; rear-delt path and weight stack are readable.
- `cable_chest_press`: 4 steps, accepted; dual cable path and stack movement are readable.
- `cable_fly`: 3 steps, accepted; dual tower path is readable.
- `cable_pullover`: 3 steps, accepted with minor soft-elbow caveat.
- `cable_glute_kickback`: 3 steps, accepted after rejecting a cropped-foot candidate.
- `cable_crunch`: 3 steps, accepted with caveat that feet are tucked/partly hidden by the kneeling pose.
- `treadmill_walk`: 3 steps, accepted; treadmill is grounded and coherent.
- `indoor_bike`: 3 steps, accepted; setup frame has one foot grounded.
- `glute_bridge`: 3 steps, accepted; controlled-lower frame is close to setup but readable.
- `hip_thrust`: 4 steps, accepted; upper-back support and top extension are clear.
- `dumbbell_step_up`: 4 steps, accepted after opposite-side rework; same box remains stable and the sequence shows right-foot setup/stand, reset, then left-foot setup. Source candidate: `docs/exercise-art-candidates/worker-lower-alternation/dumbbell_step_up_candidate_1_sheet.png`.
- `pushup`: 3 steps, accepted; straight-body lower and press are readable.
- `romanian_deadlift`: 5 steps, accepted with minor caveat that the deepest hinge has more knee bend than ideal.

Machine assets accepted before the Machine Equipment QA section was added should be rechecked for natural weight-stack movement before final PR.

## Rejected Or Unmapped Queue

These generated assets failed form-reference QA or are intentionally unmapped. Do not add app mappings until a new candidate passes the stricter checks:

- `machine_shoulder_press` previous candidates: rejected after user review because the lowered and raised phases made the machine read differently. Bad PNGs remain under `tmp/rejected-exercise-art/machine_shoulder_press_20260520/` and `tmp/rejected-exercise-art/machine_shoulder_press_v2_machine_mismatch_20260521/`; use only the accepted user-reference rework listed above.
- `barbell_bench_press` previous sheet candidates: rejected because the far arm disappeared in side view, or because multi-panel crops clipped load-bearing feet/bar ends and left neighboring-panel artifacts. Use only the individual 2026-05-21 accepted step assets.
- `assisted_pullup` previous candidate: rejected after app-scale review because the overhead handle position was unstable and the foot step/knee-pad platform appeared inconsistently between panels. Bad PNGs remain under `tmp/rejected-exercise-art/assisted_pullup_bad_machine_continuity_20260520/`; use only the reworked accepted app resources.
- `walking_lunge` previous candidates: rejected/unmapped because same-foot-only sequencing could teach the wrong alternating pattern. Bad PNGs remain under `tmp/rejected-exercise-art/walking_lunge_same_foot_20260520/`; use only the accepted alternating rework.
- `dumbbell_step_up` previous candidates: rejected/unmapped because same-foot-only sequencing could imply one-sided repetition without a clear side-change convention. Bad PNGs remain under `tmp/rejected-exercise-art/dumbbell_step_up_same_foot_20260520/`; use only the accepted opposite-side rework.

## Recheck Queue

These generated assets exist in app resources but should not be treated as final until a stricter pass:

- `conventional_deadlift` older sheet candidate: do not map the source-sheet split because rounded panel borders and bar-end crops remain. Use only the individual 2026-05-21 accepted step assets.
- `cable_lateral_raise`: generated candidate is mechanically readable, but the character face/body tone is sharper and more muscular than the canonical app character. Recheck at app-card scale before final acceptance or regenerate for brand consistency.

## Generation Workflow

1. Upload the canonical character reference sheet before prompting: `feature/training/impl/src/main/res/drawable-nodpi/trainer_exercise_sheet.png`.
2. Start from a trainer-audited movement description or verified movement reference.
3. Keep the canonical character's face, hair, outfit, body proportions, line style, and tone consistent.
4. If a movement reference is used, transform only the character, outfit, background, color tone, and camera polish into Smart Trainer style.
5. Keep the reference biomechanics intact, but avoid copying the original person's identity, clothing, background, or exact photo look.
6. Generate candidate art.
7. Run anatomy QA and exercise-specific QA.
8. Only then crop/export to `feature/training/impl/src/main/res/drawable-nodpi`.

## Prompt Template

Use this base when generating exercise art:

```text
Create polished anime fitness instruction art for Smart Trainer.
Use the attached 12-panel character reference sheet exactly for character identity and style: young male anime trainer, short spiky black hair, teal zip training jacket, black jogger pants, black training shoes with teal accents, coral wristbands.
Do not invent a new character, outfit, face, hairstyle, or body type.
Use a bright cream studio background, deep navy clean outlines, teal/cyan action accents, subtle shadows, modern mobile app guide tone.

Use the provided movement reference only for biomechanics and pose.
Do not copy the person's identity, clothing, gym background, lighting, or photo style.
Preserve the exact exercise phase and limb positions.

Anatomy QA: one head, one torso, exactly two arms, two hands, two legs, two feet; no duplicated limbs, no ghost joints, no cropped hands/feet/head.
No embedded instructional text unless explicitly requested.
```
