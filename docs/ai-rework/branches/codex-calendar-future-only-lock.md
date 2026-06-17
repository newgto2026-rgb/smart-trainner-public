# AI Rework Metrics: codex/calendar-future-only-lock

## Branch Summary
- PR: Pending
- Base: `main`
- Initial PR Commit: `HEAD`
- Latest Follow-up Commit: `HEAD`
- Scope: calendar past-through-today access correction after PR #115

## Rework Events

### USER-20260617-calendar-future-only-lock
- Source: User correction in Codex thread after PR #115 merge: dates through today should be selectable, and only future dates should be inaccessible.
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The merged calendar restriction interpreted the requirement as today-only access instead of allowing all dates up to and including today.
- Fix Scope: Restore past date selection, past month navigation, past-date add/edit/save access, and block only future dates/months.
- Fix Size: Medium
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug`, `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`, and guarded `./gradlew connectedDebugAndroidTest` on `emulator-5556`.
- Lesson: Date access requirements should distinguish "outside today" from "after today" and tests should include yesterday/tomorrow boundaries.

## External Event Coverage
- `USER-20260617-calendar-future-only-lock`: covered by rework event above

## Non-Rework Follow-up Commits
- None
