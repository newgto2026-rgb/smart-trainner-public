# Smart Trainer Visual QA - Current App

Date: 2026-05-20

Scope: visual audit only. Reviewed current screenshots under `tmp/manual-shots` and Compose/resources in `feature/training/impl`. No app source or assets were changed.

## Reviewed Evidence

- `tmp/manual-shots/smart_home_after_login.png`
- `tmp/manual-shots/smart_current.png`
- `tmp/manual-shots/smart_exercises_top.png`
- `tmp/manual-shots/smart_exercises_scrolled.png`
- `tmp/manual-shots/smart_exercises_machine_shoulder_row.png`
- `tmp/manual-shots/smart_exercises_machine_shoulder_visible.png`
- `tmp/manual-shots/smart_exercises_machine_shoulder_thumbnail_patch.png`
- `tmp/manual-shots/smart_exercises_thumbnails_resource_top_v2.png`
- `tmp/manual-shots/smart_exercises_thumbnails_resource_scrolled_v2.png`
- `tmp/manual-shots/smart_machine_shoulder_dialog_top.png`
- `tmp/manual-shots/smart_machine_shoulder_dialog_steps.png`
- `feature/training/impl/src/main/java/com/smarttrainner/feature/training/impl/TrainingRoute.kt`
- `feature/training/impl/src/main/java/com/smarttrainner/feature/training/impl/TrainerExerciseImage.kt`
- `feature/training/impl/src/main/res/values/strings.xml`
- `feature/training/impl/src/main/res/values-ko/strings.xml`

## Summary

The current app has a usable MVP structure and a calmer four-tab navigation after the record tab removal. The biggest polish issues are visual consistency rather than feature completeness: exercise thumbnails vary in crop quality, the exercise detail dialog reads as oversized on mobile and can expose a disorienting cropped hero image while scrolling, the splash/start branding does not match the in-app header, and localization needs a real Korean pass against screenshots rather than string-file parity only.

## Actionable Issues

### P1 - Exercise List Thumbnails Need a Consistent Crop System

Evidence: `smart_exercises_top.png`, `smart_exercises_scrolled.png`, `smart_exercises_machine_shoulder_row.png`, `smart_exercises_machine_shoulder_visible.png`.

Current behavior after the thumbnail-resource pass:
- `ExerciseRow` uses a fixed `72 x 80dp` thumbnail (`TrainingRoute.kt`, `ExerciseRow`) and `PlanExerciseRow` uses `76 x 84dp`.
- `TrainerExerciseImage` now prefers generated local `exercise_thumbnail_<id>.png` resources for dense cards and falls back to the step image with the older crop behavior.
- `smart_exercises_thumbnails_resource_top_v2.png` is visibly cleaner than the previous pass: leg press and goblet squat no longer show obvious step badges or source dividers, and the list feels more intentional.
- `smart_exercises_thumbnails_resource_scrolled_v2.png` still exposes source-art quality failures, especially review circles or panel marks in `hack_squat`, `smith_machine_squat`, and some older WebP-derived assets. These should be treated as source-regeneration issues, not UI layout issues.

Recommended fix:
- Define one thumbnail spec for list rows, plan rows, and home cards: same container color, size class, image scale, and clipping behavior.
- Pick the representative step per exercise intentionally. For many exercises, step 2 is not the cleanest thumbnail because it includes source annotations or awkward partial poses.
- Add a small visual QA checklist for at least: leg press, push-up, cable fly, barbell bench press, machine shoulder press, hip thrust, calf raise, incline machine press.
- If source images include central review marks, correction circles, wrong equipment, or anatomy issues, regenerate the source art. Thumbnail crops can hide corner badges but should not be used to disguise invalid exercise images.

Acceptance target:
- In a scrolled exercise list, every visible row has a clear subject, no source canvas border, no step badge, and no missing/blank thumbnail slot.

### P1 - Detail Dialog Hero Image Is Too Dominant and Scrolls Poorly

Evidence: `smart_machine_shoulder_dialog_top.png`, `smart_machine_shoulder_dialog_steps.png`.

Current behavior:
- The detail dialog fills 90% of screen height with only `18dp` side padding.
- The hero image is `216 x 240dp`, then the content starts below it. At the top it looks clean, but after scrolling the hero image can remain partially clipped at the top edge while the title begins below it, creating a visually confusing half-image state.
- The close button floats over a large white area and competes with the hero rather than anchoring the dialog header.

Recommended fix:
- Convert the detail dialog to a stable header/content structure: close action in a compact top row, image below or beside the title depending on width.
- Reduce mobile hero height or make the first image collapsible/non-sticky so scrolling begins cleanly at the title/steps.
- Consider making step cards the primary instructional content and use the hero as a smaller preview.
- Keep the CTA visible near the bottom only if the content length allows; otherwise use a bottom action area that does not cover content.

Acceptance target:
- At any scroll offset, the dialog should show either the full hero or no hero fragment. The title, chips, step cards, safety text, and CTA should remain readable without awkward cropping.

### P1 - Splash/Start Branding Does Not Match In-App Branding

Evidence: `smart_current.png`, `smart_exercises_machine_shoulder_thumbnail_patch.png`, `smart_home_after_login.png`.

Current behavior:
- The start/splash screen presents `SMART TRAINER` in all caps with a large blue-accent wordmark.
- The in-app header presents `Smart Trainer` as plain black title text inside a pale gradient card.
- The app name string is `Smart Trainer` in both English and Korean resources, while the repository and guide use `Smart Trainner` in places. Users will experience these as different products unless the naming is intentionally transitional.

Recommended fix:
- Pick a single product name spelling and capitalization for launcher/start/header/strings.
- Bring one visual brand token from the start screen into the in-app header: wordmark treatment, accent color, or compact logo mark.
- If the start screen remains all caps, consider an in-app compact wordmark instead of plain title text.

Acceptance target:
- A user moving from start screen to home should feel they stayed inside the same app, not a branded splash followed by a generic dashboard.

### P2 - Four-Tab Flow Looks Cleaner, But Record-Tab Removal Needs Regression QA

Evidence: `smart_home_after_login.png`, `smart_exercises_top.png`, `TrainingTab` contains only `HOME`, `PLAN`, `EXERCISES`, `ANALYSIS`.

Current behavior:
- Bottom navigation now has four implemented tabs and no standalone record tab, which is a good simplification.
- Record entry now happens from `Start Workout`/`Log` actions inside Home and Plan. Exercise details can also start a workout only when a planned exercise is available.
- There is no obvious visual affordance in the Exercises tab that some exercise details may not be directly loggable if the exercise is not in the plan.

Recommended fix:
- Verify these flows after any tab/navigation change: Home -> Start Workout -> save/dismiss; Home -> Plan; Plan -> Log; Exercises -> detail -> dismiss; Exercises -> planned detail -> Start Workout.
- Consider a small state-aware CTA in exercise details: `Start Workout` for planned exercises, and a non-blocking hint for unplanned catalog exercises.
- Keep tab labels short in Korean and English; current four labels fit, but `Exercises` is already the widest English tab.

Acceptance target:
- There is no dead end after removing the record tab, and users can always understand where workout logging begins.

### P2 - Korean/English Readiness Needs Screenshot-Level Validation

Evidence: `values/strings.xml`, `values-ko/strings.xml`, screenshots are currently English.

Current behavior:
- Feature strings have Korean counterparts, which is a strong baseline.
- Several strings are likely to produce layout or language-quality issues in Korean: metric pills (`연속 기록`, `부위별 완료`), duration label (`시간 분`), template/count abbreviations, and long generated exercise summaries.
- Exercise step content is localized in `ExerciseStepImages.kt`, but the dialog screenshots only demonstrate English. Korean step labels and instructions may wrap much more aggressively in the current two-column step-card layout.

Recommended fix:
- Capture the same screenshot set in Korean: home, exercises top, scrolled exercises, machine shoulder detail top, machine shoulder detail steps, record dialog.
- Check metric pill truncation, bottom tab label fit, assist chip wrapping, step-card row height, and generated summary tone.
- Rewrite `training_duration` from `시간 분` to a natural field label before release, such as `시간(분)` or `운동 시간`.
- Audit generated sentence templates so English and Korean read like trainer guidance, not placeholder text.

Acceptance target:
- Korean screens have no clipped labels, no awkward field names, and no row/card height jumps that make the UI feel less polished than English.

### P3 - Information Density Is Slightly High on Home

Evidence: `smart_home_after_login.png`.

Current behavior:
- Home repeats the same three metrics in the header and `This Week` section on the first screen.
- The current empty-progress state shows `Done today: 0/0` plus a full-width progress line, then a planned treadmill card. That combination can look contradictory.

Recommended fix:
- In empty or future-plan states, avoid showing `0/0` as the primary progress label. Use a friendlier empty state or next-session label.
- Consider reducing repeated metrics above the fold, or make the top header a compact brand/plan summary while `This Week` owns analytics.

Acceptance target:
- First launch home should make the next action obvious and avoid duplicate metrics competing for attention.

## Suggested Visual QA Checklist

Run this checklist before the next PR update that touches training UI:

1. English and Korean screenshots for Home, Plan, Exercises top, Exercises scrolled, Exercise Detail top, Exercise Detail steps, Record Dialog.
2. Thumbnail pass across at least 20 mixed equipment/bodyweight exercises.
3. Verify all bottom tabs map to implemented destinations and return to stable scroll/content state.
4. Verify dialog readability at default font and one larger Android font size.
5. Verify brand name, logo treatment, and capitalization across start screen, launcher/splash, and in-app header.

## Files To Inspect When Fixing

- `feature/training/impl/src/main/java/com/smarttrainner/feature/training/impl/TrainerExerciseImage.kt`
- `feature/training/impl/src/main/java/com/smarttrainner/feature/training/impl/TrainingRoute.kt`
- `feature/training/impl/src/main/java/com/smarttrainner/feature/training/impl/ExerciseStepImages.kt`
- `feature/training/impl/src/main/res/values/strings.xml`
- `feature/training/impl/src/main/res/values-ko/strings.xml`
## 2026-05-20 Follow-up

- Exercise detail now uses a separated dialog header and sticky record CTA instead of repeating the title inside the scroll body.
- The large detail hero uses the clean generated thumbnail resource, while the step list continues to show instruction-specific step images.
- Manual QA screenshot: `tmp/manual-shots/smart_latest_exercise_detail_dialog_after_patch.png`.
- Residual issue: several accepted step assets still contain source-sheet step badges, panel borders, or edge residue in the instruction list. These should be regenerated or re-exported from clean sources rather than hidden by UI cropping.
