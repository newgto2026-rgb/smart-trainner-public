# AI Rework Metrics: codex/google-auth-nickname

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/72
- Base: `main`
- Latest Follow-up Commit: `HEAD`
- Scope: Google sign-in account UX, nickname conflict handling, debug server configuration, and linked workout backup behavior.

## Rework Events

### R1 - Google Account Review Follow-Up
- Source: GitHub review threads `PRRT_kwDOSsEQm86F8lZR`, `PRRT_kwDOSsEQm86F8lZT`, `PRRT_kwDOSsEQm86F8lZW`, `PRRT_kwDOSsEQm86F8lZY`, and `PRRT_kwDOSsEQm86F8lZb`.
- Severity: P1.
- Attribution: Codex.
- Automation Possible: Yes.
- Automation Added: Yes.
- Status: Verified.
- Finding: The account MVP review found local workout saves could fail on backup errors, suspend cancellation could be swallowed, nickname conflict mapping was broader than needed, Google credential parsing had redundant error handling, and the debug server fallback pointed at a temporary tunnel.
- Fix Scope: Made local workout persistence authoritative when backup fails, rethrew coroutine cancellation, mapped nickname 409s inline, removed redundant Google credential parsing catch logic, and restored emulator debug fallback to `http://10.0.2.2:3001/`.
- Fix Size: Focused account/data/network update.
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:data:compileDebugKotlin :feature:workout:data:compileDebugKotlin :app:compileDebugKotlin`; `./gradlew :core:data:testDebugUnitTest :feature:workout:data:testDebugUnitTest :app:testDebugUnitTest`; `./gradlew :app:lintDebug :app:assembleDebug`
- Lesson: Account flows should preserve local-first behavior, cancellation semantics, and stable local development endpoints while keeping conflict mapping near the API call that produces it.

## External Event Coverage
- `PRRT_kwDOSsEQm86F8lZR`: covered by R1.
- `PRRT_kwDOSsEQm86F8lZT`: covered by R1.
- `PRRT_kwDOSsEQm86F8lZW`: covered by R1.
- `PRRT_kwDOSsEQm86F8lZY`: covered by R1.
- `PRRT_kwDOSsEQm86F8lZb`: covered by R1.

## Non-Rework Follow-up Commits
- None.
