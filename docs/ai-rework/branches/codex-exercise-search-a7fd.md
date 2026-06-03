# AI Rework Metrics: codex/exercise-search-a7fd

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/79
- Base: `main`
- Initial PR Commit: `4ed3cb118b3356158b38e78579c801c60b58b324`
- Latest Follow-up Commit: `HEAD`
- Scope: exercise catalog token search

## Rework Events

### PRRT_kwDOSsEQm86GonGJ
- Source: GitHub review thread on `feature/exercise/impl/src/main/java/com/smarttrainner/feature/exercise/impl/ExerciseCatalogViewModel.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: coroutine dispatcher placement is review-sensitive and covered by follow-up lint/test validation
- Status: verified
- Finding: Search filtering imports did not include `Dispatchers.Default` and `flowOn`, which were needed to move filtering work off the main thread.
- Fix Scope: Exercise catalog search flow threading
- Fix Size: Small
- Rework Commit: `3813c68385fe0fde41cf73fe416e76de3292ff9a`
- Verification: `./gradlew :feature:exercise:impl:testDebugUnitTest`; `./gradlew :feature:exercise:impl:lintDebug`
- Lesson: Search filtering that runs on user typing should explicitly separate input state responsiveness from background list work.

### PRRT_kwDOSsEQm86GonGT
- Source: GitHub review thread on `feature/exercise/impl/src/main/java/com/smarttrainner/feature/exercise/impl/ExerciseCatalogViewModel.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: performance risk is covered by unit tests for behavior and lint for implementation health, but UI jank needs code review judgment
- Status: verified
- Finding: Filtering ran inside the main `uiState` combine flow, so larger exercise catalogs could do token matching on the main dispatcher during typing.
- Fix Scope: Exercise catalog search flow threading
- Fix Size: Small
- Rework Commit: `3813c68385fe0fde41cf73fe416e76de3292ff9a`
- Verification: `./gradlew :feature:exercise:impl:testDebugUnitTest`; `./gradlew :feature:exercise:impl:lintDebug`
- Lesson: For Compose text input, keep the text state synchronous while moving derived heavy work to a background dispatcher.

### PRRT_kwDOSsEQm86GonGV
- Source: GitHub review thread on `feature/exercise/impl/src/main/java/com/smarttrainner/feature/exercise/impl/ExerciseCatalogViewModel.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: allocation shape is not covered by current lint rules, but the revised implementation is covered by behavior tests
- Status: verified
- Finding: `searchText()` allocated a temporary list and used `joinToString` for every exercise on every search query update.
- Fix Scope: Exercise catalog search text construction
- Fix Size: Small
- Rework Commit: `3813c68385fe0fde41cf73fe416e76de3292ff9a`
- Verification: `./gradlew :feature:exercise:impl:testDebugUnitTest`; `./gradlew :feature:exercise:impl:lintDebug`
- Lesson: Hot-path search helpers should avoid avoidable collection allocation when simple string composition is enough.

### actions-run-26864865304-job-79226242944
- Source: GitHub Actions check failure, `Build, Unit Test, Lint`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Not Added: existing CI already caught the cross-test coroutine leak; the fix makes the dispatcher controllable in unit tests
- Status: verified
- Finding: The search filtering flow used `Dispatchers.Default` directly, so cancellation could complete after `Dispatchers.resetMain()` and leak an uncaught Main dispatcher exception into the following ViewModel test on CI.
- Fix Scope: Exercise catalog search dispatcher testability
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:exercise:impl:testDebugUnitTest`; `./gradlew :feature:exercise:impl:lintDebug`; `./gradlew testDebugUnitTest`
- Lesson: ViewModel flows that use non-test dispatchers should expose a local test injection point or use an existing dispatcher abstraction so unit tests can fully drain background work before resetting `Dispatchers.Main`.

## External Event Coverage
- `PRRT_kwDOSsEQm86GonGJ`: covered by rework event above
- `PRRT_kwDOSsEQm86GonGT`: covered by rework event above
- `PRRT_kwDOSsEQm86GonGV`: covered by rework event above
- `actions-run-26864865304-job-79226242944`: covered by rework event above

## Non-Rework Follow-up Commits
- None
