# AI Rework Metrics: codex/routine-entry-performance-impl

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/92
- Base: `main`
- Initial PR Commit: `2aeba8b7`
- Latest Follow-up Commit: `HEAD`
- Scope: routine entry performance, routine session coordination boundaries, PR review/conflict follow-up, and CI coverage follow-up

## Rework Events

### PRRT_kwDOSsEQm86HU4V1
- Source: GitHub review thread on `feature/routine/impl/src/main/java/com/smarttrainner/feature/routine/impl/RoutineFeatureEntryImpl.kt`
- Related Threads: `PRRT_kwDOSsEQm86HU4V1`
- Severity: P1
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: lifecycle owner scope is a navigation composition decision that is better covered by compile/lint and review than a simple static check
- Status: verified
- Finding: `RoutineViewModel` was shared by walking `Context` to an Activity-level `ViewModelStoreOwner`, which kept routine state broader than the route graph.
- Fix Scope: App navigation graph scoping, routine feature route-state owner injection, and removal of Activity owner discovery helpers
- Fix Size: Medium
- Rework Commit: `ade0190e`
- Verification: `./gradlew clean :core:database:testDebugUnitTest :feature:routine:domain:test :feature:routine:impl:testDebugUnitTest :app:assembleDebug`; `./gradlew checkModuleBoundaries :core:database:lintDebug :feature:routine:api:lintDebug :feature:routine:impl:lintDebug :app:lintDebug`; `python3 /Users/kimtaenyun/.codex/skills/emulator-deploy-guard/scripts/emulator_deploy_guard.py --serial emulator-5556 --timeout 900 --note "PR 92 review cleanup connectedDebugAndroidTest" -- ./gradlew connectedDebugAndroidTest`
- Lesson: Shared feature ViewModels should receive an explicit owner from the app/navigation composition layer instead of discovering an Activity owner internally.

### PRRT_kwDOSsEQm86HU4V3
- Source: GitHub review thread on `core/database/src/main/java/com/smarttrainner/core/database/WorkoutLogDao.kt`
- Related Threads: `PRRT_kwDOSsEQm86HU4V3`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The latest-log-per-exercise DAO query used nested `DISTINCT` and `IN` subqueries that were harder to read and reason about than a correlated subquery.
- Fix Scope: `WorkoutLogDao.observeLatestByExerciseForSession` SQL shape
- Fix Size: Small
- Rework Commit: `ade0190e`
- Verification: `./gradlew clean :core:database:testDebugUnitTest :feature:routine:domain:test :feature:routine:impl:testDebugUnitTest :app:assembleDebug`; `./gradlew checkModuleBoundaries :core:database:lintDebug :feature:routine:api:lintDebug :feature:routine:impl:lintDebug :app:lintDebug`; `python3 /Users/kimtaenyun/.codex/skills/emulator-deploy-guard/scripts/emulator_deploy_guard.py --serial emulator-5556 --timeout 900 --note "PR 92 review cleanup connectedDebugAndroidTest" -- ./gradlew connectedDebugAndroidTest`
- Lesson: Query optimizations should prefer the simplest SQL shape that preserves semantics and can be covered by DAO regression tests.

### PRRT_kwDOSsEQm86HU4WH
- Source: GitHub review thread on `app/src/main/java/com/smarttrainner/app/training/TrainingRoute.kt`
- Related Threads: `PRRT_kwDOSsEQm86HU4WH`
- Severity: P1
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: module boundary checks catch feature references, but not semantic coupling to an ID string convention
- Status: verified
- Finding: App training flow checked the routine-added planned exercise ID prefix directly, coupling app logic to a routine feature implementation detail.
- Fix Scope: Routine session coordination contract and routine-owned ID helper
- Fix Size: Small
- Rework Commit: `ade0190e`
- Verification: `./gradlew clean :core:database:testDebugUnitTest :feature:routine:domain:test :feature:routine:impl:testDebugUnitTest :app:assembleDebug`; `./gradlew checkModuleBoundaries :core:database:lintDebug :feature:routine:api:lintDebug :feature:routine:impl:lintDebug :app:lintDebug`; `python3 /Users/kimtaenyun/.codex/skills/emulator-deploy-guard/scripts/emulator_deploy_guard.py --serial emulator-5556 --timeout 900 --note "PR 92 review cleanup connectedDebugAndroidTest" -- ./gradlew connectedDebugAndroidTest`
- Lesson: Cross-feature coordination contracts should expose policy-level questions, not require app code to know feature-owned ID formats.

### conflict-origin-main-2026-06-06
- Source: GitHub PR merge state `DIRTY` and local `git merge --no-commit --no-ff origin/main`
- Severity: P1
- Attribution: Mixed
- Automation Possible: Yes
- Automation Added: Existing PR mergeability check reported `DIRTY`
- Status: verified
- Finding: Latest `main` renamed weekly plan concepts to cycle plan concepts and conflicted with the routine ViewModel refresh test added in the PR.
- Fix Scope: Merge conflict resolution in `RoutineViewModelTest.kt`, preserving cycle-plan refresh behavior after main's weekly-to-cycle rename
- Fix Size: Small
- Rework Commit: `ade0190e`
- Verification: `./gradlew clean :core:database:testDebugUnitTest :feature:routine:domain:test :feature:routine:impl:testDebugUnitTest :app:assembleDebug`; `./gradlew checkModuleBoundaries :core:database:lintDebug :feature:routine:api:lintDebug :feature:routine:impl:lintDebug :app:lintDebug`; `python3 /Users/kimtaenyun/.codex/skills/emulator-deploy-guard/scripts/emulator_deploy_guard.py --serial emulator-5556 --timeout 900 --note "PR 92 review cleanup connectedDebugAndroidTest" -- ./gradlew connectedDebugAndroidTest`
- Lesson: Performance tests around plan refresh should track the current domain vocabulary so stacked architecture changes merge cleanly.

### ci-android-ci-27065767472-ui-excluded-coverage
- Source: GitHub Actions `Android CI / Build, Unit Test, Lint` run `27065767472`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The review follow-up introduced `PlannedExerciseId.isRoutineAdditionalExerciseId()` in a 100% domain coverage gate file without a focused unit test, causing `:uiExcludedTestCoverageVerification` to fail with line coverage `0.9`.
- Fix Scope: Routine command use case test coverage for routine additional exercise ID detection
- Fix Size: Small
- Rework Commit: HEAD
- Verification: `./gradlew :feature:routine:domain:test uiExcludedTestCoverageVerification`
- Lesson: Domain helpers added to `*UseCase*` files must be covered immediately because CI enforces 100% line coverage for the UI-excluded domain gate.

## External Event Coverage
- `PRRT_kwDOSsEQm86HU4V1`: covered by rework event above
- `PRRT_kwDOSsEQm86HU4V3`: covered by rework event above
- `PRRT_kwDOSsEQm86HU4WH`: covered by rework event above
- `conflict-origin-main-2026-06-06`: covered by rework event above
- `ci-android-ci-27065767472-ui-excluded-coverage`: covered by rework event above

## Non-Rework Follow-up Commits
- None
