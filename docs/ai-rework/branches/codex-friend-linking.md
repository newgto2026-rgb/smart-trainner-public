# AI Rework Metrics: codex/friend-linking

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/101
- Base: `main`
- Initial PR Commit: `11b5ce1fb44ceb779f234b32aaca11328b2cd3db`
- Latest Follow-up Commit: `f4ad9ce01663ed0a9f82f4d7bda4de5b7706a768`
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
- Rework Commit: `f01b593b366068e7f46c8085c7909f0b9bf68dd1`
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
- Rework Commit: `f01b593b366068e7f46c8085c7909f0b9bf68dd1`
- Verification: `./gradlew :feature:friend:api:assembleDebug :feature:friend:impl:lintDebug :app:compileDebugKotlin :app:assembleDebugAndroidTest`; `./gradlew :app:lintDebug`; `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`; `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.smarttrainner.TrainingUiTest#friendsTabOpensFriendRoute`
- Lesson: App-level push plumbing needs defensive handling around platform services and Firebase task failures before feature-level flows depend on it.

### PRRT-gemini-friend-data-and-ui-review-followup
- Source: Gemini Code Assist unresolved PR review threads
- Severity: P1
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: the issues span repository failure semantics, network concurrency, keyboard UX, and Android lint resource formatting, which are better covered by targeted unit/lint/build plus instrumentation validation in this branch.
- Status: verified
- Finding: Several active review threads remained unresolved after the initial push hardening: friend mutations could report failure when only the post-mutation cache refresh failed, friend refresh fetched independent network resources sequentially, nickname input auto-capitalized handles, and request status mapping plus subtitle placeholders could still trigger review/lint concerns.
- Fix Scope: Friend data repository mutation/refresh behavior, friend mapper test coverage, Friends input keyboard behavior, and friend string resources.
- Fix Size: Small
- Rework Commit: `f4ad9ce01663ed0a9f82f4d7bda4de5b7706a768`
- Verification: `./gradlew :feature:friend:data:testDebugUnitTest`; `./gradlew :feature:friend:data:lintDebug :feature:friend:impl:lintDebug :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`; guarded `adb -s emulator-5556 shell am instrument -w -r com.smarttrainner.test/com.smarttrainner.HiltTestRunner` returned `OK (24 tests)`. A broad `./gradlew :app:connectedDebugAndroidTest` run reached `24/24` pass on `emulator-5556` and the Galaxy mDNS connection, but the duplicate Galaxy USB connection crashed one instrumentation process and made the aggregate Gradle task fail.
- Lesson: Review cleanup needs thread-level verification instead of relying on a summary review body; connected tests also need a single-device path when a physical device is exposed twice over USB and mDNS.

### ci-27113994995-notification-permission-dialog
- Source: GitHub Actions failing check `Connected UI Tests`
- Severity: P0
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Adding app-level FCM permission prompting caused a fresh API 35 CI emulator to show the Android notification permission dialog on startup, blocking the Compose login/home screen and timing out all 23 connected tests.
- Fix Scope: Android instrumentation test permission setup
- Fix Size: Small
- Rework Commit: `80da176887a5594c7e3b4b6133944154cf10c436`
- Verification: `./gradlew :app:compileDebugAndroidTestKotlin`; `./gradlew :app:connectedDebugAndroidTest`
- Lesson: App-level runtime permissions need explicit instrumentation-test setup so system permission UI cannot mask app UI and make feature tests fail for a shared startup precondition.

## External Event Coverage
- `conversation-2026-06-08-friends-tab-crash`: covered by rework event above
- `ci-27112865567-register-push-token-coverage`: covered by rework event above
- `PRR_kwDOSsEQm88AAAABCPY-DQ`: covered by `PRR_kwDOSsEQm88AAAABCPY-DQ-fcm-stability` and `PRRT-gemini-friend-data-and-ui-review-followup`
- `ci-27113994995-notification-permission-dialog`: covered by rework event above

## Non-Rework Follow-up Commits
- `f305103382778f8198f2a76267ba83188905610a`: Firebase Android app config requested during setup
- `15a9d18b0696658d89e6245de95bc8149d0efe33`: Firebase registration documentation requested during setup
- `7b1bf83a337ac7a89fd456b099924f65b45b8955`: latest `origin/main` merge and Friends tab alignment with the new top-level back handling
