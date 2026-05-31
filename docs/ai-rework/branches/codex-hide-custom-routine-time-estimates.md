# AI Rework Metrics: codex/hide-custom-routine-time-estimates

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/71
- Base: `main`
- Initial PR Commit: `22adcbbb194049d7a2ff4785f49f9525026ed0ea`
- Latest Follow-up Commit: `HEAD`
- Scope: custom routine time estimate visibility

## Rework Events

### PRRT_kwDOSsEQm86F72Td
- Source: GitHub review thread on `feature/routine/impl/src/main/java/com/smarttrainner/feature/routine/impl/RoutineHomeSummaryContent.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The custom routine label condition was widened in a way that made system routines skip their detailed subtitle with focus and estimated duration.
- Fix Scope: Routine home summary UI condition and Android UI regression coverage
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:routine:impl:testDebugUnitTest`; `./gradlew :feature:routine:impl:lintDebug`; `./gradlew :app:assembleDebugAndroidTest`; `./gradlew :app:lintDebug :feature:routine:impl:lintDebug`
- Lesson: When hiding metadata for custom routines, keep system routine visibility covered by a direct regression assertion instead of relying only on custom-routine absence checks.

## External Event Coverage
- `PRRT_kwDOSsEQm86F72Td`: covered by rework event above

## Non-Rework Follow-up Commits
- None
