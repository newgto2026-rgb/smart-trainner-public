# Smart Trainner Agent Guide

## Source Of Truth
- GitHub app repo: `newgto2026-rgb/smart-trainner-public`.
- The `origin` remote for this checkout should point to `https://github.com/newgto2026-rgb/smart-trainner-public.git`.
- Companion server repo: `/Users/kimtaenyun/server/smart-trainner` (`newgto2026-rgb/smart-trainner-server`).
- When Android `core:network` request/response DTOs or API paths change, update and verify the companion server repo in the same task unless the user explicitly excludes server work.

## New Task Workflow
- Start every new task from the latest public `main` by running `scripts/new-smart-task <task-name>`.
- The helper fetches `origin main`, creates `codex/<task-name>`, and checks it out at `$HOME/.codex/worktrees/<task-name>/smart-trainner`.
- If you create a worktree manually, fetch first and base the branch on `origin/main`, not on a stale local `main`.

```sh
git fetch origin main
git worktree add -b codex/<task-name> "$HOME/.codex/worktrees/<task-name>/smart-trainner" origin/main
```

## Required Pre-Change Checks
1. Confirm the current branch is not `main` or `master`.
2. Confirm new work is based on fresh `origin/main`.
3. Identify affected Gradle modules.
4. Open affected module-local `AGENTS.md` files when they exist.
5. Check whether the change affects server API contracts and, if so, inspect `/Users/kimtaenyun/server/smart-trainner/AGENTS.md`.
6. Check module boundaries and dependency direction before editing.

## Required Pre-PR Checks
1. Run affected module unit tests.
2. Run affected module lint.
3. Run app build or integration tests when the change crosses module boundaries.
4. Run `connectedDebugAndroidTest` before PR updates when core user flows or UI test harnesses changed.
5. Record validation commands and results in the PR description.

## Architecture And Dependencies
- Keep module boundaries and dependency direction intact.
- `core:*` must not depend on `feature:*`.
- `feature:*` depends on domain use cases and public models, not data implementations.
- Shared, cross-feature repository contracts live in `core:domain`; their shared implementations live in `core:data`.
- Feature-owned repository contracts, policies, and use cases live in that feature's `feature:*:domain`; their implementations may live in that feature's approved `feature:*:data`.
- `app` owns final DI composition. App production code may reference implementation/data/domain classes only from approved app-owned DI modules.
- DTO, database, and DataStore models are not domain models.
- Network contracts belong in `core:network`; Room contracts belong in `core:database`.

## UI And Resources
- User-visible strings belong in `values` and `values-ko`, except explicit seed/domain content.
- Compose UI renders `UiState` and sends user events; keep business and application logic out of composables.
- Top-level tabs must map only to implemented feature routes and use explicit icons.

## Verification Commands
- App build: `./gradlew :app:assembleDebug`
- Debug androidTest APK: `./gradlew :app:assembleDebugAndroidTest`
- All Android unit tests: `./gradlew testDebugUnitTest`
- JVM unit tests: `./gradlew test`
- Instrumented UI tests: `./gradlew connectedDebugAndroidTest`
- Module unit test: `./gradlew :<module>:testDebugUnitTest`
- JVM module test: `./gradlew :<module>:test`
- Module lint: `./gradlew :<module>:lintDebug`
- Final Android gate: `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`
