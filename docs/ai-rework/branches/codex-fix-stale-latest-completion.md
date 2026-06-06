# AI Rework Metrics: codex/fix-stale-latest-completion

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/96
- Base: `origin/main`
- Initial PR Commit: `1c096fb22245b3321e8009e738775e5ca9fee165`
- Latest Follow-up Commit: `HEAD`
- Scope: routine cycle cursor policy, routine switching data retention, analysis/routine UX copy, Android UI regression coverage, companion server routine-progress contract

## Rework Events

### USER-CYCLE-SWITCH-CURRENT-CYCLE-ONLY
- Source: User correction: routine switching must delete only the in-progress cycle, not completed past cycles.
- Severity: P1
- Attribution: Mixed
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Resetting routine progress to cycle 1 or deleting all previous workout history would violate the completed-cycle retention policy.
- Fix Scope: Routine switch cursor policy, current-cycle workout log deletion scope, server switch-template contract, UI regression coverage
- Fix Size: Large
- Rework Commit: `1c096fb22245b3321e8009e738775e5ca9fee165`
- Verification: `./gradlew :core:database:testDebugUnitTest :core:datastore:testDebugUnitTest :feature:routine:data:testDebugUnitTest :feature:routine:impl:testDebugUnitTest :app:assembleDebugAndroidTest`; `env ANDROID_SERIAL=RFCT32G6YLN ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest`; server `npm test -- routineProgress routes`; server `npm run lint`
- Lesson: Routine template changes must preserve completed-cycle history and reset only the active cycle cursor.

### USER-CYCLE-COMPLETE-CONFIRMATION
- Source: User correction: completing the last routine day must always require confirmation before starting the next cycle.
- Severity: P1
- Attribution: Mixed
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Last-day completion could advance to the next cycle without a dedicated user confirmation in the unrecorded-exercise path.
- Fix Scope: Routine completion dialog reason, latest-completion cursor reset, ViewModel completion policy, UI tests for recorded and unrecorded last-day flows
- Fix Size: Medium
- Rework Commit: `1c096fb22245b3321e8009e738775e5ca9fee165`
- Verification: `./gradlew :feature:routine:impl:testDebugUnitTest`; `env ANDROID_SERIAL=RFCT32G6YLN ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest`
- Lesson: Cycle-boundary actions are user-visible state transitions and need an explicit confirmation gate even when the normal day-completion path would be automatic.

### USER-UI-TEST-COVERAGE
- Source: User request: keep the policy regression coverage as UI tests.
- Severity: P1
- Attribution: Human
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Unit tests alone would not prove the user-facing home/routine/analysis behavior after routine switching and cycle completion.
- Fix Scope: Android instrumented UI scenarios for custom routine day-one progress, current-cycle deletion, completed-cycle retention, analysis recent-record visibility, and cycle-completion confirmation
- Fix Size: Medium
- Rework Commit: `1c096fb22245b3321e8009e738775e5ca9fee165`
- Verification: `env ANDROID_SERIAL=RFCT32G6YLN ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest`
- Lesson: Data-retention policy should be asserted through the same screens that make the policy understandable to users.

### PRRT_kwDOSsEQm86Hk1ag
- Source: GitHub review thread on `feature/routine/data/src/main/java/com/smarttrainner/feature/routine/data/DefaultRoutineProgressRepository.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: the ordering is covered by targeted code review and the routine-switch UI regression path, while this data repository does not currently have a focused fake-network unit harness
- Status: verified
- Finding: `switchRoutineTemplate` deleted local current-cycle logs and updated preferences before the server confirmed the routine switch, which could desynchronize the client if the server rejected or failed the request.
- Fix Scope: Routine switch network/local write ordering
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:routine:data:testDebugUnitTest :feature:routine:data:lintDebug`; `./gradlew :core:database:testDebugUnitTest :core:datastore:testDebugUnitTest :feature:routine:impl:testDebugUnitTest :app:assembleDebugAndroidTest`; `env ANDROID_SERIAL=emulator-5556 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest`
- Lesson: Destructive local cache updates for server-owned state should happen only after server confirmation.

### PRRT_kwDOSsEQm86Hk1ai
- Source: GitHub review thread on `feature/routine/data/src/main/java/com/smarttrainner/feature/routine/data/DefaultRoutineProgressRepository.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The zero-argument `effectiveCycleStartedAt()` helper used `ZoneId.systemDefault()`, making fallback cycle start resolution environment-dependent despite an injected `Clock`.
- Fix Scope: Clock-zone consistency in routine progress mapping
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:routine:data:testDebugUnitTest :feature:routine:data:lintDebug`; `./gradlew :core:database:testDebugUnitTest :core:datastore:testDebugUnitTest :feature:routine:impl:testDebugUnitTest :app:assembleDebugAndroidTest`; `env ANDROID_SERIAL=emulator-5556 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest`
- Lesson: Production date fallback logic should use injected time sources rather than process-global timezone defaults.

## External Event Coverage
- User cycle-switch policy corrections: covered by `USER-CYCLE-SWITCH-CURRENT-CYCLE-ONLY`
- User cycle-completion confirmation correction: covered by `USER-CYCLE-COMPLETE-CONFIRMATION`
- User UI-test coverage request: covered by `USER-UI-TEST-COVERAGE`
- `PRRT_kwDOSsEQm86Hk1ag`: covered by rework event above
- `PRRT_kwDOSsEQm86Hk1ai`: covered by rework event above

## Non-Rework Follow-up Commits
- `27f7f1fdd8e4574b69858371eee472459c5b7a7f`: Update branch metrics document with PR URL and initial PR commit hash.
