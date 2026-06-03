## Branch Summary
- Branch: `codex/training-muscle-roles-main`
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/80
- Base: `main`
- Initial PR commits: `693fa3ea`, `7be88878`, `037e7c25`
- Current rework status: `verified`

## Rework Events

### PRRT_kwDOSsEQm86GqWUP
- Source: GitHub review thread
- Reviewer: `gemini-code-assist`
- Severity: `P1`
- Attribution: `AI`
- Automation Possible: `Yes`
- Automation Added: `Yes`
- Status: `verified`
- Issue: Workout record prefilling could read initial empty flows and miss all-time planned-exercise history.
- Fix Scope: Restore async planned-exercise latest-log lookup with token protection and keep clean defaults for different planned exercises.
- Fix Size: `medium`
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:workout:impl:testDebugUnitTest :feature:routine:impl:testDebugUnitTest :feature:exercise:impl:testDebugUnitTest :app:assembleDebug` passed; `./gradlew :core:database:lintDebug :feature:workout:data:lintDebug :feature:workout:impl:lintDebug :feature:routine:data:lintDebug :feature:routine:impl:lintDebug :feature:exercise:impl:lintDebug :app:lintDebug :app:assembleDebugAndroidTest` passed; `./gradlew connectedDebugAndroidTest` passed on `emulator-5556`.
- Lesson: Prefill logic should use all-time data and protect user-edited form state from late async updates.

### PRRT_kwDOSsEQm86GqWUR
- Source: GitHub review thread
- Reviewer: `gemini-code-assist`
- Severity: `P1`
- Attribution: `AI`
- Automation Possible: `Yes`
- Automation Added: `Yes`
- Status: `verified`
- Issue: Saving and starting a custom routine ignored routine-switch failure and closed the builder anyway.
- Fix Scope: Keep builder open and surface save failure when post-save switch fails.
- Fix Size: `small`
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:workout:impl:testDebugUnitTest :feature:routine:impl:testDebugUnitTest :feature:exercise:impl:testDebugUnitTest :app:assembleDebug` passed; `./gradlew :core:database:lintDebug :feature:workout:data:lintDebug :feature:workout:impl:lintDebug :feature:routine:data:lintDebug :feature:routine:impl:lintDebug :feature:exercise:impl:lintDebug :app:lintDebug :app:assembleDebugAndroidTest` passed; `./gradlew connectedDebugAndroidTest` passed on `emulator-5556`.
- Lesson: Multi-step save/start flows must treat each step failure as user-visible state, not just the first persistence result.

### PRRT_kwDOSsEQm86GqWUe
- Source: GitHub review thread
- Reviewer: `gemini-code-assist`
- Severity: `P2`
- Attribution: `AI`
- Automation Possible: `Partial`
- Automation Added: `Yes`
- Status: `verified`
- Issue: Exercise catalog sorting used enum ordinal, making future enum reordering silently change product order.
- Fix Scope: Add explicit `sortRank` to `ExerciseMovementPattern` and use it in catalog/routine sorting.
- Fix Size: `small`
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:workout:impl:testDebugUnitTest :feature:routine:impl:testDebugUnitTest :feature:exercise:impl:testDebugUnitTest :app:assembleDebug` passed; `./gradlew :core:database:lintDebug :feature:workout:data:lintDebug :feature:workout:impl:lintDebug :feature:routine:data:lintDebug :feature:routine:impl:lintDebug :feature:exercise:impl:lintDebug :app:lintDebug :app:assembleDebugAndroidTest` passed; `./gradlew connectedDebugAndroidTest` passed on `emulator-5556`.
- Lesson: Product sort semantics should be encoded as explicit data rather than enum declaration position.

### PRRT_kwDOSsEQm86GqWUg
- Source: GitHub review thread
- Reviewer: `gemini-code-assist`
- Severity: `P2`
- Attribution: `AI`
- Automation Possible: `Partial`
- Automation Added: `Not Added: data repository has no focused unit-test harness yet; covered by code review and integration checks`
- Status: `verified`
- Issue: Routine template switch wrote raw server progress to preferences, then immediately wrote merged progress, causing redundant emissions.
- Fix Scope: Let `pushServerProgress` skip intermediate preference writes for switch flow so only final merged progress is emitted.
- Fix Size: `small`
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:workout:impl:testDebugUnitTest :feature:routine:impl:testDebugUnitTest :feature:exercise:impl:testDebugUnitTest :app:assembleDebug` passed; `./gradlew :core:database:lintDebug :feature:workout:data:lintDebug :feature:workout:impl:lintDebug :feature:routine:data:lintDebug :feature:routine:impl:lintDebug :feature:exercise:impl:lintDebug :app:lintDebug :app:assembleDebugAndroidTest` passed; `./gradlew connectedDebugAndroidTest` passed on `emulator-5556`.
- Lesson: Sync helpers that write observable state need opt-out hooks when callers must merge server snapshots first.

## External Event Coverage
- `PRRT_kwDOSsEQm86GqWUP`: captured as rework event.
- `PRRT_kwDOSsEQm86GqWUR`: captured as rework event.
- `PRRT_kwDOSsEQm86GqWUe`: captured as rework event.
- `PRRT_kwDOSsEQm86GqWUg`: captured as rework event.

## Non-Rework Follow-up Commits
- None yet.
