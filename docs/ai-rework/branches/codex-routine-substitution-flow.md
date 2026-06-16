# AI Rework Metrics: codex/routine-substitution-flow

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/106
- Base: `main`
- Initial PR Commit: `d87f536493a4fa53194aa0431e12ea44f1e46b62`
- Latest Follow-up Commit: `HEAD`
- Scope: routine substitution and active custom-routine edit progress policy

## Rework Events

### PRRT_kwDOSsEQm86JzmjS
- Source: GitHub review thread on `feature/routine/impl/src/main/java/com/smarttrainner/feature/routine/impl/RoutineViewModel.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: existing ViewModel and UI tests cover the active-cycle prompt behavior, while stale cross-cycle date retention is reviewer-identified state-shape robustness
- Status: verified
- Finding: `hasCurrentCycleProgress()` checked `progress.routineDayDates.isNotEmpty()`, which could treat retained dates from another cycle as current-cycle progress.
- Fix Scope: Active custom-routine edit progress detection
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:routine:impl:testDebugUnitTest --tests com.smarttrainner.feature.routine.impl.RoutineViewModelTest`; `./gradlew :feature:routine:impl:lintDebug :app:lintDebug`
- Lesson: Current-cycle UI policy should derive date assignment from the resolved current cycle plan, not from raw persisted progress maps that may contain broader history.

## External Event Coverage
- `PRRT_kwDOSsEQm86JzmjS`: covered by rework event above

## Non-Rework Follow-up Commits
- None
