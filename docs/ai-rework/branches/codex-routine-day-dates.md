# AI Rework Metrics: codex/routine-day-dates

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/87
- Base: `main`
- Initial PR Commit: `0445d075`
- Latest Follow-up Commit: `HEAD`
- Scope: routine day date assignment, workout log date policy, cycle summary, and completed routine-day state

## Rework Events

### PRRT_kwDOSsEQm86HQCa0
- Source: GitHub review thread on `feature/analysis/domain/src/main/java/com/smarttrainner/feature/analysis/domain/WeeklySummaryCalculator.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Cycle summary filtering could include routine logs from other templates or cycles when their timestamp was after the current cycle start.
- Fix Scope: Split routine-day log filtering from ad-hoc timestamp filtering in cycle summary calculation.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:analysis:domain:test :feature:routine:domain:test :feature:routine:data:testDebugUnitTest`; `./gradlew :feature:routine:data:lintDebug`; `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`
- Lesson: Routine-scoped IDs should be treated as authoritative scope keys before using broad timestamp windows.

### PRRT_kwDOSsEQm86HQCa5
- Source: GitHub review thread on `feature/routine/domain/src/main/java/com/smarttrainner/feature/routine/domain/RoutinePolicyUseCases.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Routine cycle completion could include routine logs from other templates or cycles through the timestamp fallback.
- Fix Scope: Split routine-day log filtering from ad-hoc timestamp filtering in cycle completion resolution.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:analysis:domain:test :feature:routine:domain:test :feature:routine:data:testDebugUnitTest`; `./gradlew :feature:routine:data:lintDebug`; `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`
- Lesson: Completion resolution needs the same cycle identity guard as reporting paths.

### PRRT_kwDOSsEQm86HQCa7
- Source: GitHub review thread on `feature/routine/data/src/main/java/com/smarttrainner/feature/routine/data/DefaultRoutineProgressRepository.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Day-one assigned dates were converted using UTC day start, which can exclude morning local-time workouts in positive-offset timezones.
- Fix Scope: Calculate assigned day-one cycle start with the provided local zone and add data-module coverage for Asia/Seoul.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:analysis:domain:test :feature:routine:domain:test :feature:routine:data:testDebugUnitTest`; `./gradlew :feature:routine:data:lintDebug`; `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`
- Lesson: Date-only user choices must be converted with the app/user local zone, not UTC, before comparing local workout timestamps.

## External Event Coverage
- `PRRT_kwDOSsEQm86HQCa0`: covered by rework event above
- `PRRT_kwDOSsEQm86HQCa5`: covered by rework event above
- `PRRT_kwDOSsEQm86HQCa7`: covered by rework event above

## Non-Rework Follow-up Commits
- None
