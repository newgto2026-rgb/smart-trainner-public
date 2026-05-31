# AI Rework Metrics: codex/exercise-image-async-loading-main

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/69
- Base: `main`
- Initial PR Commit: `28605600ea635ad0e4bd575f24c54a161e7577d6`
- Latest Follow-up Commit: `HEAD`
- Scope: route exercise image rendering through Coil while preserving lightweight design system assets

## Rework Events

### PRRT_kwDOSsEQm86F7hYT
- Source: GitHub review thread on `core/designsystem/build.gradle.kts`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: module ownership preference is architectural review feedback; compile and lint validate dependency placement after the change
- Status: verified
- Finding: The PR added `coil-compose` to `:core:designsystem` and changed static brand assets to `AsyncImage`, coupling the design system to a third-party image loader for assets that are not part of the heavy exercise image surface.
- Fix Scope: Dependency placement cleanup
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:designsystem:lintDebug :core:exercise-media:testDebugUnitTest :core:exercise-media:lintDebug :app:assembleDebug`
- Lesson: Keep third-party image loading at the heavy media boundary; static design system brand assets can stay on the platform Compose image APIs.

## External Event Coverage
- `PRRT_kwDOSsEQm86F7hYT`: covered by rework event above

## Non-Rework Follow-up Commits
- None
