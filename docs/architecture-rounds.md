# Smart Trainner Architecture Review Rounds

Last updated: 2026-05-20 KST

## Round 1 - Current-State Diagnosis
- Android had a healthy core split: `core:model`, `core:domain`, `core:data`, `core:database`, `core:datastore`, `core:network`, `core:designsystem`, and `core:testing`.
- The weak point was feature ownership: `app` directly imported the concrete `TrainingRoute`, so the app shell knew implementation details.
- The repository contract is still broad as one `TrainingRepository`; acceptable for MVP, but the next pressure point is splitting catalog, plan, workout log, and analytics contracts.
- Server API routes cover the MVP surface, but Android remote DTO coverage is still partial. Local-first behavior remains correct for MVP.

## Round 2 - Pro-Split Argument
- `feature:training` should be split early while the surface is small.
- `api` should hold stable app-facing contracts only.
- `entry` should hold Hilt/app wiring only.
- `impl` should hold ViewModel, UI state, resources, generated imagery, and Compose implementation.
- This prevents future app/navigation work from importing implementation classes.

## Round 3 - Anti-Split Argument
- Three modules cost more than one module: Gradle config, hooks, AGENTS, CI, and cognitive overhead.
- There is only one feature today, so a deep feature split such as weekly-plan/catalog/session/history/analytics would be premature.
- The compromise is to split contract/entry/implementation, but keep the training feature family unified.

## Round 4 - Decision
- Adopt `:feature:training:api`, `:feature:training:entry`, and `:feature:training:impl`.
- Do not split into multiple training subfeatures yet.
- `app` depends on `api` and `entry`; `entry` binds `api` to `impl`; `impl` depends on domain/model/design system.
- Hooks, AGENTS, and docs must refer to the submodules.

## Round 5 - Acceptance Criteria
- `app` must not import `com.smarttrainner.feature.training.impl.*`.
- `:feature:training:api` must not depend on impl, entry, data, database, network, Room, Retrofit, or DataStore.
- `:feature:training:entry` must not contain UI or business logic.
- `:feature:training:impl` must not depend on `core:data`.
- Verification: `./gradlew :feature:training:api:lintDebug :feature:training:entry:lintDebug :feature:training:impl:lintDebug :app:assembleDebug`.

## Follow-Up Auditor Pass
- Architect audit confirmed `app` imports `TrainingFeatureEntry` from `:feature:training:api` and does not directly depend on training impl.
- `:feature:training:entry` is the only expected place that imports `:feature:training:impl` and binds it through Hilt.
- Exercise detail dialog state remains inside `:feature:training:impl`, so the public feature contract stays stable.
- Current architectural risk: `:feature:training:impl:testDebugUnitTest` has no source tests yet; UI behavior is covered primarily through instrumented app tests.
