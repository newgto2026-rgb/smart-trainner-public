# AI Rework Metrics: codex/calendar-today-only

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/115
- Base: `main`
- Initial PR Commit: `bed5145477668c8fe0b12515bc831885c86c7701`
- Latest Follow-up Commit: `HEAD`
- Scope: calendar today-only access, calendar editor stale-date handling, and review follow-up

## Rework Events

### PRRT_kwDOSsEQm86KP9ZQ
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/CalendarViewModel.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Calendar editor save could silently return if midnight passed while a today draft was open and the draft date was no longer the current day.
- Fix Scope: Surface `CalendarWorkoutEditorError.SAVE_FAILED` for stale editor drafts and cover the midnight rollover path with a mutable-clock ViewModel test.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug` and `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`.
- Lesson: Time-gated UI actions should report stale-state failure to the user instead of treating the action as a no-op.

### PRRT_kwDOSsEQm86KP9ZV
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/CalendarViewModel.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Calendar day selection still derived from selected date, which could diverge from the only accessible day if selected-date state lagged behind today.
- Fix Scope: Derive `isSelected` from `isAccessible` and extend the saved non-today date ViewModel test to assert only today is selected.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug` and `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`.
- Lesson: When a grid intentionally has one accessible cell, selected state should be derived from that access policy.

## External Event Coverage
- `PRRT_kwDOSsEQm86KP9ZQ`: covered by rework event above
- `PRRT_kwDOSsEQm86KP9ZV`: covered by rework event above

## Non-Rework Follow-up Commits
- None
