# AI Rework Metrics: codex/modularization-routine-route-blackbox

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/29
- Base: `codex/modularization-routine-api-tightening`
- Initial PR Commit: `2041a8baa36d2a47bfbce9d1d4b4feb76d667b10`
- Latest Follow-up Commit: `HEAD`
- Scope: routine route API boundary tightening

## Rework Events

### PRRT_kwDOSsEQm86F4coS
- Source: GitHub review thread on `feature/routine/api/src/main/java/com/smarttrainner/feature/routine/api/RoutineModels.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Not Added: API shape preference is review-sensitive and already covered by compile checks
- Status: verified
- Finding: `RoutineRouteState.currentRoutineName()` was exposed as a composable function even though it only returns a derived string.
- Fix Scope: API surface cleanup
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew checkModuleBoundaries :feature:routine:api:compileDebugKotlin :feature:routine:impl:compileDebugKotlin :app:compileDebugKotlin`; `./gradlew :feature:routine:api:lintDebug :feature:routine:impl:lintDebug :app:lintDebug`; `./gradlew :feature:routine:impl:testDebugUnitTest :app:testDebugUnitTest`
- Lesson: Opaque feature route state should expose derived values as plain read-only properties unless composition is actually required.

### PRRT_kwDOSsEQm86F4coT
- Source: GitHub review thread on `feature/routine/impl/src/main/java/com/smarttrainner/feature/routine/impl/RoutineFeatureEntryImpl.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Not Added: compile checks verify interface implementation after the API shape change
- Status: verified
- Finding: The routine route implementation kept the unnecessary composable function override.
- Fix Scope: Implementation cleanup
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew checkModuleBoundaries :feature:routine:api:compileDebugKotlin :feature:routine:impl:compileDebugKotlin :app:compileDebugKotlin`; `./gradlew :feature:routine:api:lintDebug :feature:routine:impl:lintDebug :app:lintDebug`; `./gradlew :feature:routine:impl:testDebugUnitTest :app:testDebugUnitTest`
- Lesson: Implementation details should match the narrowest public API contract needed by app orchestration.

### PRRT_kwDOSsEQm86F4coV
- Source: GitHub review thread on `app/src/main/java/com/smarttrainner/app/training/TrainingRoute.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Not Added: app compile checks verify call-site compatibility
- Status: verified
- Finding: The app call site invoked `currentRoutineName()` instead of reading the route state's derived property.
- Fix Scope: App call-site cleanup
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew checkModuleBoundaries :feature:routine:api:compileDebugKotlin :feature:routine:impl:compileDebugKotlin :app:compileDebugKotlin`; `./gradlew :feature:routine:api:lintDebug :feature:routine:impl:lintDebug :app:lintDebug`; `./gradlew :feature:routine:impl:testDebugUnitTest :app:testDebugUnitTest`
- Lesson: App-owned routing should consume feature route state as a plain contract and avoid unnecessary Compose entry points.

## External Event Coverage
- `PRRT_kwDOSsEQm86F4coS`: covered by rework event above
- `PRRT_kwDOSsEQm86F4coT`: covered by rework event above
- `PRRT_kwDOSsEQm86F4coV`: covered by rework event above

## Non-Rework Follow-up Commits
- None
