# AI Rework Metrics: codex/friend-linking

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/101
- Base: `main`
- Initial PR Commit: `11b5ce1fb44ceb779f234b32aaca11328b2cd3db`
- Latest Follow-up Commit: `HEAD`
- Scope: friend linking feature, app-level FCM push token registration, friend notifications, and server API integration

## Rework Events

### conversation-2026-06-08-friends-tab-crash
- Source: User correction after emulator install
- Severity: P0
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Tapping the Friends tab crashed because the public friend feature API exposed a composable route without applying the Compose compiler plugin, causing an ABI mismatch at runtime.
- Fix Scope: Friend API module Compose configuration and Friends tab regression coverage
- Fix Size: Small
- Rework Commit: `42ba7027a7ba36af4479c871cb494b283e753064`
- Verification: `./gradlew :feature:friend:api:assembleDebug :app:assembleDebug :app:assembleDebugAndroidTest`; `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest#friendsTabOpensFriendRoute`; manual emulator Friends tab launch with empty `AndroidRuntime` crash log
- Lesson: A newly wired top-level tab needs a connected navigation smoke test before handoff, especially when its route crosses module API boundaries.

### ci-27112865567-register-push-token-coverage
- Source: GitHub Actions failing check `Build, Unit Test, Lint`
- Severity: P0
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: `RegisterPushTokenUseCase` was added under `core:domain`, matched the 100% domain use case coverage gate, and had no unit test, so `uiExcludedTestCoverageVerification` failed.
- Fix Scope: Push token use case unit coverage
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:domain:test --rerun-tasks`; `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`
- Lesson: New `*UseCase` classes in gated domain modules need success and failure tests in the same change that introduces them.

### PRR_kwDOSsEQm88AAAABCPY-DQ-fcm-stability
- Source: Gemini Code Assist PR review
- Severity: P1
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: these are Android service lifecycle and Firebase task failure paths that are best validated by compile/lint plus manual FCM smoke in this branch
- Status: verified
- Finding: FCM token retrieval read `task.result` before checking task success, the messaging service owned an uncancelled coroutine scope, notification manager lookup was not null-safe, notification IDs could collide, and notification taps navigated to Friends without forcing a server refresh on an already-alive route.
- Fix Scope: App-level FCM registrar, friend notification delivery hardening, and notification-triggered Friends refresh
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:friend:api:assembleDebug :feature:friend:impl:lintDebug :app:compileDebugKotlin :app:assembleDebugAndroidTest`; `./gradlew :app:lintDebug`; `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`; `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest#friendsTabOpensFriendRoute`
- Lesson: App-level push plumbing needs defensive handling around platform services and Firebase task failures before feature-level flows depend on it.

## External Event Coverage
- `conversation-2026-06-08-friends-tab-crash`: covered by rework event above
- `ci-27112865567-register-push-token-coverage`: covered by rework event above
- `PRR_kwDOSsEQm88AAAABCPY-DQ`: covered by rework event above

## Non-Rework Follow-up Commits
- `f305103382778f8198f2a76267ba83188905610a`: Firebase Android app config requested during setup
- `15a9d18b0696658d89e6245de95bc8149d0efe33`: Firebase registration documentation requested during setup
