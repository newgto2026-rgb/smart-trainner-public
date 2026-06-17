# AI Rework Metrics: codex/workout-history-edit

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/114
- Base: `main`
- Initial PR Commit: `801685132c7b954c1657802517bbfd8505e9e0e0`
- Latest Follow-up Commit: `HEAD`
- Scope: calendar workout history add/edit, workout log sync path, UI tests, and review follow-up

## Rework Events

### PRRT_kwDOSsEQm86KN1o-
- Source: GitHub review thread on `core/data/src/main/java/com/smarttrainner/core/data/DefaultWorkoutLogRepository.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Successful network workout-log mutations could be reported as failures if the local synced marker update threw afterward.
- Fix Scope: Isolate post-network `markSynced` writes from network mutation results.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :core:domain:test :core:data:testDebugUnitTest :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug uiExcludedTestCoverageVerification`, `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`, `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`, and guarded `./gradlew connectedDebugAndroidTest`.
- Lesson: Sync-safe writes need separate failure boundaries for remote mutation and local cache state updates.

### PRRT_kwDOSsEQm86KN1pD
- Source: GitHub review thread on `feature/calendar/impl/src/main/res/values/strings.xml`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The calendar set-number string used `%d`, which can trigger Android Lint plural-candidate warnings for sentence strings.
- Fix Scope: Use a `%s` placeholder and pass a preformatted set number.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :core:domain:test :core:data:testDebugUnitTest :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug uiExcludedTestCoverageVerification`, `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`, `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`, and guarded `./gradlew connectedDebugAndroidTest`.
- Lesson: Sentence resources that are not plurals should avoid integer placeholders when lint suggests pluralization.

### PRRT_kwDOSsEQm86KN1pI
- Source: GitHub review thread on `feature/calendar/impl/src/main/res/values-ko/strings.xml`
- Severity: P2
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The Korean calendar set-number string used `%d`, matching the same plural-candidate risk.
- Fix Scope: Use a `%s` placeholder and pass a preformatted set number.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :core:domain:test :core:data:testDebugUnitTest :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug uiExcludedTestCoverageVerification`, `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`, `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`, and guarded `./gradlew connectedDebugAndroidTest`.
- Lesson: Keep localized placeholder types aligned with lint-safe base resources.

### PRRT_kwDOSsEQm86KN1pN
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/components/CalendarWorkoutEditorDialog.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Yes
- Status: verified
- Finding: The calendar workout editor dialog had a fixed max height that could exceed smaller or landscape screens.
- Fix Scope: Cap dialog height by the smaller of `640.dp` and 85% of the current screen height.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :core:domain:test :core:data:testDebugUnitTest :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug uiExcludedTestCoverageVerification`, `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`, `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`, and guarded `./gradlew connectedDebugAndroidTest`.
- Lesson: Dialog height limits should be responsive when the content is scrollable.

### GHA-27690105365-ui-excluded-coverage
- Source: GitHub Actions run `27690105365`, check `Build, Unit Test, Lint`, step `Run JVM unit tests`.
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The CI-only `uiExcludedTestCoverageVerification` gate failed because `UpdateWorkoutLogUseCase.invoke` was included in the 100% use-case gate and JaCoCo reported its suspend expression-body entry line as missed.
- Fix Scope: Add core-domain unit coverage for `UpdateWorkoutLogUseCase` delegation and align the coroutine wrapper with the existing use-case gate exclusions.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: Passed `./gradlew :core:domain:test :core:data:testDebugUnitTest :feature:calendar:impl:testDebugUnitTest :feature:calendar:impl:lintDebug uiExcludedTestCoverageVerification`, `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`, `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`, and guarded `./gradlew connectedDebugAndroidTest`.
- Lesson: New suspend command wrappers need focused delegation tests and, when JaCoCo still misses the coroutine entry line, the same explicit gate treatment as existing suspend wrappers.

## External Event Coverage
- `PRRT_kwDOSsEQm86KN1o-`: covered by rework event above
- `PRRT_kwDOSsEQm86KN1pD`: covered by rework event above
- `PRRT_kwDOSsEQm86KN1pI`: covered by rework event above
- `PRRT_kwDOSsEQm86KN1pN`: covered by rework event above
- `GHA-27690105365-ui-excluded-coverage`: covered by rework event above

## Non-Rework Follow-up Commits
- None
