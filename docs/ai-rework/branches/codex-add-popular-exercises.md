# AI Rework Metrics: codex/add-popular-exercises

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/68
- Base: `main`
- Initial PR Commit: `6ec46aafd20ffed43e04fec2ed903975a2aac5ed`
- Latest Follow-up Commit: `HEAD`
- Scope: popular exercise catalog entries and exercise art assets

## Rework Events

### USER-2026-05-31-image-framing-scale
- Source: User correction in Codex thread after PR creation
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: visual centering still needs human review, but the background and canvas-size checks were rerun
- Status: verified
- Finding: Some generated exercise images had tight head framing, off-center character placement, or inconsistent character scale across cuts.
- Fix Scope: Exercise art extraction and framing
- Fix Size: Medium
- Rework Commit: `543a07415cd54efd6613ecbefd8db26d8dc5a9fb`
- Verification: `./gradlew :core:exercise-media:testDebugUnitTest`; image asset check confirmed 25 files, expected background `#FAF6ED`, bad=0
- Lesson: Generated exercise art should preserve one source-sheet scale per exercise, use overlap extraction for panel-boundary subjects, and be contact-sheet reviewed for top padding, centering, and character scale before the first PR commit.

### PRRT_kwDOSsEQm86F7XWp
- Source: GitHub review thread on `core/domain/src/main/java/com/smarttrainner/core/domain/SeedTrainingContent.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: Korean spelling nuance is review-sensitive and not covered by current lint rules
- Status: verified
- Finding: The Bulgarian split squat seed text used `뒤발`, `뒤발등`, `뒤무릎`, and `뒤발로` instead of the preferred `뒷발` forms.
- Fix Scope: Korean localization wording
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:domain:test :core:exercise-media:testDebugUnitTest`
- Lesson: New Korean exercise copy should be checked against nearby catalog wording for spacing and spelling conventions before PR.

### PRRT_kwDOSsEQm86F7XWr
- Source: GitHub review thread on `core/exercise-media/src/main/java/com/smarttrainner/core/exercisemedia/ExerciseStepImages.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: Korean spelling nuance is review-sensitive and not covered by current lint rules
- Status: verified
- Finding: The Bulgarian split squat step visual copy used `뒤발등` and `뒤무릎` instead of the preferred `뒷발등` and `뒷무릎`.
- Fix Scope: Korean localization wording
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:domain:test :core:exercise-media:testDebugUnitTest`
- Lesson: Seed exercise instructions and step visual instructions should be reviewed together so wording fixes stay synchronized.

### PRRT_kwDOSsEQm86F7XWu
- Source: GitHub review thread on `core/domain/src/main/java/com/smarttrainner/core/domain/SeedTrainingContent.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: label style consistency is review-sensitive and not covered by current lint rules
- Status: verified
- Finding: The barbell Romanian deadlift seed instruction title used `무릎 살짝 굽힘`, which did not match the `-기` title style used around it.
- Fix Scope: Korean localization wording
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:domain:test :core:exercise-media:testDebugUnitTest`
- Lesson: Step titles should be checked as a set for suffix consistency, not only for literal instruction meaning.

### PRRT_kwDOSsEQm86F7XWx
- Source: GitHub review thread on `core/exercise-media/src/main/java/com/smarttrainner/core/exercisemedia/ExerciseStepImages.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: label style consistency is review-sensitive and not covered by current lint rules
- Status: verified
- Finding: The barbell Romanian deadlift step visual label and instruction used `무릎 살짝 굽힘` instead of `무릎 살짝 굽히기`.
- Fix Scope: Korean localization wording
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:domain:test :core:exercise-media:testDebugUnitTest`
- Lesson: Step visual labels should mirror seed instruction titles after copy edits.

## External Event Coverage
- `USER-2026-05-31-image-framing-scale`: covered by rework event above
- `PRRT_kwDOSsEQm86F7XWp`: covered by rework event above
- `PRRT_kwDOSsEQm86F7XWr`: covered by rework event above
- `PRRT_kwDOSsEQm86F7XWu`: covered by rework event above
- `PRRT_kwDOSsEQm86F7XWx`: covered by rework event above

## Non-Rework Follow-up Commits
- None
