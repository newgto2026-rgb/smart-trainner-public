# Smart Trainner UI/UX Review Rounds

Last updated: 2026-05-20 KST

## Round 1 - First-Time User Journey
- The first screen should prioritize "today's workout" over analytics.
- Showing 0% completion before the user starts feels evaluative too early.
- CTA copy should feel like coaching, so "운동 시작" is better than "기록하기".

## Round 2 - In-Gym Usability
- Users need fast access to the current exercise, target sets/reps, and method images.
- Pure text-field recording is still friction-heavy, but acceptable for MVP if the selected exercise and defaults are obvious.
- Exercise instructions should be reachable from the workout context without forcing users to hunt through a long list.

## Round 3 - Navigation And Information Architecture
- Five tabs are acceptable for MVP only if each tab maps to a clear task.
- `Record` may eventually become an active-workout mode rather than a top-level tab.
- Exercise detail should not appear as an arbitrary first item on Exercise tab entry.

## Round 4 - Visual And Accessibility
- Status icons need semantic descriptions, not color alone.
- Plan template selection should expose description, level, and days per week instead of only compact chips.
- Exercise imagery is useful, but reused sprite panels are an MVP compromise and should be expanded over time.

## Round 5 - Implemented MVP Adjustments
- Home now shows today's workout before weekly summary.
- Plan templates render as explanation cards with selected state.
- Exercise tab no longer auto-selects the first exercise.
- Exercise selection opens a large scrollable dialog so users do not need to scroll back to the top for details.
- Step image UI tests now select an exercise before expecting detail images.
- Completion status icons include accessibility content descriptions.

## Round 6 - Real Screenshot QA
- Actual emulator screenshots showed the old image treatment stretched portrait artwork into landscape containers and made the source look pixelated.
- The exercise sprite sheet is now higher resolution, image rendering preserves source aspect ratio, and large home/detail placements were reduced to stop over-scaling.
- Duplicate English labels in artwork are cropped at render time because Korean exercise names already exist in the UI.
- The step-phase red dot was removed because it visually collided with the character; phase cards now rely on text labels and a bottom progress stripe.
- The dialog includes an icon close affordance and remains internally scrollable.

## Round 7 - Plan-Native Recording
- User feedback challenged the standalone Record tab: recording is not a destination, it is the next action from the current plan.
- Bottom navigation now stays at four durable destinations: Home, Plan, Exercise, and Analysis.
- Plan exercise rows expose a clear `기록` action, and tapping it opens a scrollable dialog with the selected exercise, planned target, rest, relevant input fields, and a bridge to `운동 방법 보기`.
- Duration-only cardio hides irrelevant reps and weight fields, keeping the dialog closer to the user's actual logging task.
- The record dialog now wraps to its content up to a scrollable max height, avoiding the empty-page feel seen in screenshot QA.
- `운동 방법 보기` switches to the exercise detail dialog and returns to the active record dialog when dismissed, avoiding stacked dialogs while preserving record context.
- Latest emulator QA checked the plan screen and record dialog screenshots under `/tmp/smart-trainner-screens`.

## Round 8 - Variable Set Recording
- User feedback clarified that actual work cannot assume 4 sets, 5 sets, or one weight value for the whole exercise.
- The record dialog now treats each set as its own row with independent repetitions, weight, or duration fields.
- Users can add or remove sets up to a practical MVP limit of 12; added sets copy the previous set so progressive warm-up or top-set work is quick to enter.
- The primary save action appears before the memo field so a 3-5 set workout can be logged without hunting through optional notes.
- The data model stores per-set records and keeps aggregate fields only as compatibility/summary helpers, which keeps later analysis ready for changing weight, missed reps, and variable set counts.

## Round 9 - English-First Locale Support
- User feedback clarified that English support should exist from the start, not as a later retrofit.
- Default Android strings now render in English, while Korean strings stay in `values-ko` for Korean devices.
- The feature UI localizes exercise names, plan names, target text, enum labels, day labels, and weekly insight copy at display time.
- Korean seed content remains the source for Korean users; English users receive generated English exercise guidance and safety cues instead of raw Korean text.
- UI tests now assert the default English surface so accidental fallback to Korean is caught.
