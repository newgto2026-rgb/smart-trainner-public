# Modularization Phases

This plan follows the Android modularization guidance: keep modules highly cohesive, minimize coupling, expose only the API needed by other modules, and let the app module own app startup and root navigation.

## Target Shape

The final architecture should make the app module a thin composition layer:

- `:app` owns application startup, session gating, root navigation, and top-level chrome.
- `:core:*` modules provide reusable models, domain contracts, data implementations, design tokens, and shared UI primitives.
- `:feature:*:api` modules expose stable route keys and public contracts only.
- `:feature:*:impl` modules own screen implementation, ViewModels, and feature-specific UI.
- `:feature:*:entry` modules bind implementation contracts for the app without leaking implementation classes.

## Phase 1: App Shell And Shared UI Foundation

Status: in progress on `codex/modularization-guide-refactor`.

- Introduce app-owned top-level navigation.
- Replace the broad `TrainingFeatureEntry.Content()` handoff with a destination-aware feature contract.
- Add `:core:ui` for domain-free shared Compose primitives.
- Remove top-level tab state from `TrainingViewModel`.

This phase intentionally keeps the training implementation in one Gradle feature module while making the top-level destinations explicit.

## Phase 2: Routine Feature Boundary

Status: stacked after Phase 1 on `codex/modularization-routine-feature`.

Candidate modules:

- `:feature:routine:api`
- `:feature:routine:entry`
- `:feature:routine:impl`

Move routine selection, routine settings, recommendations, custom routine builder, and current routine schedule screens behind a routine feature contract. Keep shared routine domain models in `:core:model` and routine use cases in `:core:domain` until a stronger domain split is justified.

First PR scope:

- Introduce `:feature:routine:api`.
- Move routine-only UI contract state, actions, and form errors out of `:feature:training:impl`.
- Pass the routine destination through `RoutineUiState` and `RoutineActions` instead of the full training state.
- Keep `TrainingViewModel` as the temporary coordinator until routine screen implementation can be moved safely.

## Phase 3: Exercise Catalog Feature Boundary

Status: stacked after Phase 2 on `codex/modularization-exercise-feature`.

Candidate modules:

- `:feature:exercise:api`
- `:feature:exercise:entry`
- `:feature:exercise:impl`

Move exercise list, exercise detail, exercise images, and exercise instruction rendering behind an exercise catalog feature contract. Keep exercise image assets local to the exercise implementation unless another feature requires direct access.

First PR scope:

- Introduce `:feature:exercise:api`.
- Move exercise catalog destination input to `ExerciseCatalogUiState` and `ExerciseCatalogActions`.
- Keep exercise detail and image rendering in `:feature:training:impl` until the screen implementation can move without depending on training state.

## Phase 4: Analysis Feature Boundary

Status: stacked after Phase 3 on `codex/modularization-analysis-feature`.

Candidate modules:

- `:feature:analysis:api`
- `:feature:analysis:entry`
- `:feature:analysis:impl`

Move weekly summary, completion metrics, muscle balance, and insight UI behind an analysis feature contract. The feature should depend on domain use cases, not training UI state.

First PR scope:

- Introduce `:feature:analysis:api`.
- Move analysis destination input to `AnalysisUiState`.
- Move recent workout log presentation models behind the analysis contract.
- Keep analysis UI implementation in `:feature:training:impl` until the screen implementation can move without depending on training composition state.

## Phase 5: Workout Recording Flow Boundary

Status: stacked after Phase 4 on `codex/modularization-workout-feature`.

Candidate modules:

- `:feature:workout:api`
- `:feature:workout:entry`
- `:feature:workout:impl`

Move workout start, record dialog, set entry form, save flow, and workout log row rendering behind a workout feature contract. This phase should decide whether recording is a top-level destination or an internal flow launched from routine and exercise features.

First PR scope:

- Introduce `:feature:workout:api`.
- Move workout recording form state, form errors, recording UI state, and recording actions into the workout contract.
- Pass the record dialog through `WorkoutRecordingUiState` and `WorkoutRecordingActions` instead of the full training state.
- Keep save/prefill orchestration in `TrainingViewModel` until routine and exercise implementations no longer launch recording through training composition state.

## Phase 6: Dependency And DI Cleanup

Status: stacked after Phase 5 on `codex/modularization-boundary-guardrails`.

- Reduce direct `:app` dependencies on storage and network implementation modules where Hilt aggregation allows it.
- Introduce a composition module only if it removes real app dependency noise without hiding runtime requirements.
- Add module dependency checks so feature implementations cannot depend on each other directly.

First PR scope:

- Add a root `checkModuleBoundaries` verification task.
- Fail builds when `core:*` depends on `feature:*`.
- Fail builds when feature modules depend on data/storage/network implementations.
- Fail builds when feature implementations or entries depend on other feature implementations/entries directly.
- Wire boundary checks into lint tasks so normal quality gates catch dependency direction regressions.

## Phase 7: Analysis Implementation Module

Status: stacked after Phase 6 on `codex/modularization-analysis-impl`.

Move the analysis destination implementation out of `:feature:training:impl` after its API contract exists. The training feature may provide the analysis state while it remains the temporary coordinator, but it should call only the analysis API entry point.

First PR scope:

- Introduce `:feature:analysis:impl` and `:feature:analysis:entry`.
- Add `AnalysisFeatureEntry` to `:feature:analysis:api`.
- Move analysis UI and analysis-specific resources into `:feature:analysis:impl`.
- Register the analysis entry binding in Hilt and have `:app` include the entry module.
- Keep `:feature:training:impl` depending only on `:feature:analysis:api`, not the analysis implementation.

## Phase 8: Exercise Detail Contract

Status: stacked after Phase 7 on `codex/modularization-exercise-detail-contract`.

Define the exercise catalog and detail entry contracts before moving the large exercise implementation and media assets. This keeps the API reviewable and avoids a temporary bridge where training pretends to implement the exercise feature.

First PR scope:

- Add Compose entry contracts to `:feature:exercise:api` for catalog content and exercise detail dialog rendering.
- Add `ExerciseDetailUiState` and `ExerciseDetailActions` without exposing `PlannedExercise` or training-specific state.
- Keep exercise images, step text, formatter policy, and detail UI implementation in `:feature:training:impl` until the next implementation-move PR.
- Document that exercise-specific media and detail rendering belong to `:feature:exercise:impl`, while only domain-free primitives belong in `:core:ui`.

Next PR scope:

- Introduce `:feature:exercise:impl` and `:feature:exercise:entry`.
- Move exercise detail UI, image resources, step text, and media mapping into the exercise implementation.
- Replace the first training detail call site with `ExerciseDetailFeatureEntry`.

## Phase 9: Exercise Detail Implementation Module

Status: stacked after Phase 8 on `codex/modularization-exercise-detail-impl`.

Move exercise detail and media rendering out of `:feature:training:impl` now that the contract exists. Training remains a temporary coordinator for exercise catalog state, routine state, and record launch state, but it should consume exercise-owned UI through the exercise API entry points.

First PR scope:

- Introduce `:feature:exercise:impl` and `:feature:exercise:entry`.
- Bind `ExerciseDetailFeatureEntry` and the neutral `ExerciseMediaRenderer` from the exercise entry module.
- Move exercise detail UI, step image rendering, generated media mappings, and `drawable-nodpi` exercise images into `:feature:exercise:impl`.
- Remove exercise detail-only resources and image implementation references from `:feature:training:impl`.
- Have training call `:feature:exercise:api` for exercise detail and a neutral core UI renderer for exercise media.

Next PR scope:

- Move the exercise catalog destination content behind `ExerciseCatalogFeatureEntry`.
- Decide whether catalog state stays in the temporary training coordinator for one more PR or moves with a feature-owned ViewModel.

## Phase 10: Exercise Catalog Implementation Move

Status: stacked after Phase 9 on `codex/modularization-exercise-catalog-impl`.

Move the exercise catalog destination UI out of `:feature:training:impl` so the exercise feature owns both list and detail rendering. Training remains the temporary state coordinator for one more step, but the destination body should be composed through the exercise API contract only.

First PR scope:

- Bind `ExerciseCatalogFeatureEntry` from the exercise entry module.
- Move exercise list sections, row rendering, empty state copy, equipment labels, and latest-record display formatting into `:feature:exercise:impl`.
- Remove exercise catalog-only UI resources and helpers from `:feature:training:impl`.
- Keep `TrainingViewModel` producing `ExerciseCatalogUiState` and `ExerciseCatalogActions` until the next state-ownership phase.

Next PR scope:

- Move routine implementation behind `RoutineFeatureEntry`, or move workout recording implementation behind `WorkoutRecordingFeatureEntry` if it is the safer dependency cut.
- Continue shrinking `:feature:training:impl` toward a temporary coordinator with no feature-owned screen rendering.

## Phase 11: Workout Recording Implementation Module

Status: stacked after Phase 10 on `codex/modularization-workout-recording-impl`.

Move workout recording dialog rendering out of `:feature:training:impl` now that recording state and actions already live in `:feature:workout:api`. Training still owns save orchestration and selected exercise coordination, but it should call the workout feature entry point for the recording UI.

First PR scope:

- Add `:feature:workout:impl` and `:feature:workout:entry`.
- Add `WorkoutRecordingFeatureEntry` to `:feature:workout:api`.
- Move record dialog UI, field labels, record validation messages, and recording display formatting into `:feature:workout:impl`.
- Keep `TrainingViewModel` producing `WorkoutRecordingUiState` and `WorkoutRecordingActions` until routine/workout state ownership can move safely.
- Have `:feature:training:impl` depend only on `:feature:workout:api`, not workout implementation.

Next PR scope:

- Move routine destination rendering behind a routine feature entry, or split workout save orchestration if routine dependencies still make that safer.
- Audit remaining `training` resources to ensure only coordinator/home/routine-specific copy remains there.

## Phase 12: Routine Implementation Module

Status: stacked after Phase 11 on `codex/modularization-routine-impl`.

Move routine tab rendering and routine management overlays out of `:feature:training:impl` behind the existing routine state/actions contract. Training still owns the temporary `TrainingViewModel` orchestration, but routine UI composition now belongs to the routine feature module.

First PR scope:

- Add `:feature:routine:impl` and `:feature:routine:entry`.
- Add `RoutineFeatureEntry` to `:feature:routine:api`.
- Move routine tab content, routine library/settings/recommendation dialogs, and custom routine builder UI into `:feature:routine:impl`.
- Have `:feature:training:impl` inject and call the routine feature entry instead of rendering routine screens directly.
- Keep the home screen's next-routine summary in training for now because it still coordinates workout start and day completion.

Next PR scope:

- Decide whether the home screen should become its own `feature:home`/dashboard contract or whether routine owns the next-routine summary widget.
- Continue reducing `TrainingViewModel` into orchestration-only pieces by moving routine state ownership behind a routine ViewModel/use-case boundary.

## Phase 13: Routine Home Summary Ownership

Status: stacked after Phase 12 on `codex/modularization-routine-home-summary`.

Move the routine-owned home summary UI out of `:feature:training:impl`. The home tab still uses the temporary training coordinator, but the next-routine card, progress badges, copy, and completion error display now live behind the routine feature contract.

First PR scope:

- Add a home summary renderer to `RoutineFeatureEntry`.
- Move the next routine day card and its home-only strings into `:feature:routine:impl`.
- Pass workout start and routine-day completion as routine actions instead of rendering the card in training.
- Remove the now-unused `:core:ui` dependency and home summary strings from `:feature:training:impl`.

Next PR scope:

- Move top-level destination ownership out of `:feature:training:api` so `:app` owns route metadata and destination selection directly.
- Split the temporary `TrainingViewModel` into feature-owned state holders, starting with routine state and workout recording state.
- Revisit `TrainingRepository` as multiple domain contracts once feature ViewModels no longer require a single all-purpose repository surface.

## Phase 14: App-Owned Destination Selection

Status: stacked after Phase 13 on `codex/modularization-app-owned-destinations`.

Move route metadata and top-level destination selection out of `:feature:training:api`. The app module should own tab routes, labels, icons, test tags, the root `NavHost`, and the destination `when`. Training remains a temporary coordinator surface only because feature-owned ViewModels and app-level feature composition are not split yet.

First PR scope:

- Remove `TrainingDestination` from `:feature:training:api`.
- Move tab labels from `:feature:training:api` resources into app resources.
- Expose separate `TrainingFeatureEntry.Home`, `Routine`, `Exercises`, and `Analysis` surfaces so app decides which destination to render.
- Remove destination branching from `TrainingRoute`; it now accepts destination content from its caller.
- Document that DI is still not fully app-owned: current Hilt bindings live in `:feature:*:entry` modules and are aggregated because `:app` depends on those entries.

Next PR scope:

- Replace the broad training facade with app-level injection of each feature entry where possible.
- Move Hilt feature-entry binding modules into an app-owned composition root, or introduce an explicit app composition module, so the app is the place where feature implementations are assembled.
- Remove cross-feature API references where the coupling is only for presentation composition, especially `routine/api -> exercise/api` and `workout/api -> exercise/api`.
- Split `TrainingViewModel` so each feature owns its state and only app-level flows coordinate between features.

## Phase 15: Feature Isolation Guardrail

Status: stacked after Phase 14 on `codex/modularization-feature-isolation-guard`.

Make cross-feature awareness explicit in the build instead of letting it spread silently. The codebase is not yet at strict feature ignorance, so this phase adds a small transitional allowlist and fails any new cross-feature API dependency outside that list.

First PR scope:

- Extend `checkModuleBoundaries` to detect cross-feature API dependencies.
- Allow only the current transitional cross-feature API edges.
- Keep direct feature implementation and entry dependencies blocked.
- Document that each allowlisted edge needs an owner-removal plan before strict feature isolation is complete.

Current transitional allowlist after Phase 18:

- Empty. New cross-feature API dependencies fail `checkModuleBoundaries`.

Next PR scope:

- Move app-level feature entry injection out of the broad `TrainingFeatureEntry` facade where the current shared `TrainingViewModel` permits it.
- Decide whether exercise media should remain an exercise feature service consumed through app composition, or move to a neutral core presentation contract that does not make routine/workout APIs depend on exercise API.
- Add a failing guard once the transitional allowlist is empty.

## Phase 16: Analysis State Ownership

Status: stacked after Phase 15 on `codex/modularization-analysis-viewmodel`.

Move the analysis tab from training-coordinated state to a feature-owned route and ViewModel. This is the first destination where app directly routes to a feature entry instead of going through the broad training facade.

First PR scope:

- Add `AnalysisFeatureEntry.Route()` and an `AnalysisViewModel` in `:feature:analysis:impl`.
- Have app inject `AnalysisFeatureEntry` directly and render the analysis destination through that entry.
- Remove analysis state construction from `TrainingViewModel` and `TrainingUiState`.
- Remove `:feature:training:impl -> :feature:analysis:api` from dependencies and the transitional allowlist.

Next PR scope:

- Repeat the feature-owned route/ViewModel pattern for exercise catalog or routine, depending on which can be separated from workout recording with the smallest coordination surface.
- Continue moving feature entry injection into app as each destination stops requiring the training coordinator.

## Phase 17: Exercise Media Core UI Port

Status: stacked after Phase 16 on `codex/modularization-exercise-media-core-ui`.

Remove cross-feature awareness where routine and workout only needed exercise media rendering. Exercise image rendering is now exposed as a neutral core UI port, while the exercise feature remains the implementation owner.

First PR scope:

- Add `ExerciseMediaRenderer` to `:core:ui`.
- Have `:feature:exercise:impl` implement the renderer and `:feature:exercise:entry` bind it.
- Replace routine and workout API usage of the exercise feature media contract with `ExerciseMediaRenderer`.
- Remove `:feature:routine:* -> :feature:exercise:api` and `:feature:workout:* -> :feature:exercise:api` from Gradle dependencies and the transitional allowlist.

Next PR scope:

- Split the app-owned training coordinator into feature-owned route/ViewModels now that feature modules no longer depend on each other.
- Move Hilt feature-entry bindings into an app-owned composition root if the entry modules become unnecessary.

## Phase 18: App-Owned Training Coordinator

Status: stacked after Phase 17 on `codex/modularization-app-training-coordinator`.

Remove `:feature:training:*` as a feature module because it was no longer a cohesive feature. Its remaining responsibility was cross-feature orchestration for home/routine/exercises and workout recording. That responsibility now lives in `:app`, which is allowed to know feature contracts as the routing and composition control tower.

First PR scope:

- Move the temporary `TrainingViewModel`, coordinator routes, mappers, resources, and unit tests from `:feature:training:impl` into `:app`.
- Remove `:feature:training:api`, `:feature:training:entry`, and `:feature:training:impl` from Gradle settings and app dependencies.
- Inject exercise, routine, workout, and analysis feature entries directly in `MainActivity`.
- Render home/routine/exercises through app-owned coordinator route functions instead of a broad `TrainingFeatureEntry` facade.
- Empty the cross-feature API dependency allowlist.

Next PR scope:

- Move exercise catalog/detail state from the app coordinator into `:feature:exercise:impl`.
- Move routine state and custom routine state from the app coordinator into `:feature:routine:impl`.
- Move workout recording save/prefill state from the app coordinator into `:feature:workout:impl`.

## Phase 19: Workout Recording State Ownership

Status: stacked after Phase 18 on `codex/modularization-workout-recording-viewmodel`.

Move workout recording form state out of the app-owned training coordinator and into the workout feature. App still chooses when the recording dialog is shown and receives a save-complete callback, but reps/weight/duration/rest/memo state, validation, latest-log prefill, and save orchestration now belong to `:feature:workout:impl`.

First PR scope:

- Add `WorkoutRecordingViewModel` in `:feature:workout:impl`.
- Move record form mapping, validation, default set prefill, and save input construction from app into workout impl.
- Add `WorkoutRecordingFeatureEntry.DialogRoute(...)` so app passes only the selected `PlannedExercise`, save callback, exercise-detail callback, dismiss callback, and core media renderer.
- Remove workout recording form state and actions from `TrainingViewModel` and `TrainingUiState`.
- Add workout ViewModel unit coverage for latest-log prefill and save orchestration.

Next PR scope:

- Move exercise catalog/detail state from the app coordinator into `:feature:exercise:impl`.
- Move routine state and custom routine state from the app coordinator into `:feature:routine:impl`.
- Revisit `:feature:*:entry` Hilt bindings once feature-owned routes no longer require coordinator-mediated state.

## Phase 20: Exercise Detail State Ownership

Status: stacked after Phase 19 on `codex/modularization-exercise-detail-route`.

Move exercise detail dialog state out of the app-owned training coordinator. App still chooses which exercise id is selected and whether the current routine context can launch recording, but exercise detail now loads the exercise and latest log inside `:feature:exercise:impl`.

First PR scope:

- Add `ExerciseDetailFeatureEntry.DialogRoute(...)`.
- Add `ExerciseDetailViewModel` in `:feature:exercise:impl`.
- Remove app construction of `ExerciseDetailUiState` and latest-log lookup for detail.
- Keep app-owned `selectedExerciseId` only as cross-feature flow selection until catalog and routine state are moved.
- Add exercise detail ViewModel unit coverage.

Next PR scope:

- Move routine state and custom routine builder state into `:feature:routine:impl`.
- Revisit `:feature:*:entry` Hilt bindings once feature-owned routes no longer require coordinator-mediated state.

## Phase 21: Exercise Catalog State Ownership

Status: stacked after Phase 20 on `codex/modularization-exercise-catalog-viewmodel`.

Move exercise catalog state construction out of the app-owned training coordinator. App still owns the selected exercise id because it coordinates catalog selection with exercise detail and workout recording, but exercise list data and latest-log display inputs now come from `:feature:exercise:impl`.

First PR scope:

- Add `ExerciseCatalogViewModel` in `:feature:exercise:impl`.
- Add `ExerciseCatalogFeatureEntry.rememberUiState(...)` so app can embed catalog content in the existing shared `LazyListScope` shell without constructing catalog state.
- Remove `ExerciseCatalogUiState` from `TrainingUiState` and `TrainingViewModel`.
- Keep app-owned `selectedExerciseId` only as the cross-feature selection handle until app-level flow coordination is reduced further.
- Add exercise catalog ViewModel unit coverage.

Next PR scope:

- Revisit the shared training shell so feature routes can own full list surfaces instead of providing `LazyListScope` content.
- Revisit `:feature:*:entry` Hilt bindings once feature-owned routes no longer require coordinator-mediated state.

## Phase 22: Routine State Ownership

Status: stacked after Phase 21 on `codex/modularization-routine-viewmodel`.

Move routine tab, home summary, recommendation, completion, and custom routine builder state out of the app-owned coordinator. App still coordinates cross-feature exercise detail and workout recording launch, but routine data loading, form state, day completion, and custom routine save orchestration now belong to `:feature:routine:impl`.

First PR scope:

- Add `RoutineViewModel` in `:feature:routine:impl`.
- Add `RoutineFeatureEntry.rememberRouteState(...)` so app can embed the existing home/routine `LazyListScope` content while routine owns its state and actions.
- Move routine mappers and routine ViewModel tests from app to routine impl.
- Shrink app `TrainingViewModel` to selected exercise id and recording flow coordination only.
- Add app coordinator unit coverage for recording flow selection and routine continuation.

Next PR scope:

- Revisit the shared training shell so feature routes can own full list surfaces instead of providing `LazyListScope` content.
- Revisit `:feature:*:entry` Hilt bindings once feature-owned routes no longer require coordinator-mediated state.

## Phase 23: Routine API Tightening And Data Split Audit

Status: stacked after Phase 22 on `codex/modularization-routine-api-tightening`.

Tighten the routine feature contract before adding more modules. The routine API should expose only what app needs for routing and cross-feature handoffs. Routine-only policy helpers stay in `:feature:routine:impl`.

First PR scope:

- Move custom routine muscle-group eligibility out of `:feature:routine:api` and into `:feature:routine:impl`.
- Keep app unaware of routine-only builder policy.
- Record the current decision on `feature:*:domain`, `feature:*:network`, and `feature:*:data` modules.

Split decision:

- Do not add `:feature:*:network` modules now. Network contracts and DTOs belong in `:core:network`; features should not depend on network implementation details.
- Shared repository contracts stay in `:core:domain`, with shared implementations in `:core:data`.
- Feature-private repository contracts may live in `:feature:<name>:domain` and their implementations in `:feature:<name>:data` when only that feature consumes the contract and the data source is not a shared app capability.
- Do not add `:feature:*:data` modules for the current code yet. The existing repository surface is shared by routine, exercise, workout, analysis, app session state, tests, and the app-owned coordinator.
- Do not add `:feature:*:domain` modules yet. `:feature:routine:domain` is the first plausible candidate if routine-only policy keeps growing, but today the stronger move is to keep public feature API small and split the broad core repository surface first.
- If data pressure grows, split `TrainingRepository` in `:core:domain` into purpose-specific shared contracts such as exercise, routine, workout log, and session repositories, then split implementation classes inside `:core:data`. If a contract is proven feature-private after that split, promote it to feature-local domain/data in a separate PR.

Next PR scope:

- Revisit the shared training shell so feature routes can own full list surfaces instead of providing `LazyListScope` content.
- Revisit `:feature:*:entry` Hilt bindings once feature-owned routes no longer require coordinator-mediated state.
- Split broad core-domain repository contracts before introducing feature-owned data/domain/network modules.

## Strict Feature Isolation Audit

Current state is strict at the feature-module dependency level. State ownership is now feature-owned for the major destination and dialog surfaces, with app keeping only cross-feature coordination:

- `:feature:analysis`, `:feature:exercise`, `:feature:routine`, and `:feature:workout` no longer depend on another feature's API, implementation, or entry module.
- `:app` knows the feature APIs and entries because it owns routing and cross-feature composition.
- App `TrainingViewModel` now coordinates only selected exercise id and workout recording continuation across routine/exercise/workout; workout recording, exercise detail, exercise catalog, analysis, routine, and custom routine builder state are feature-owned.
- `:feature:*:entry` modules own Hilt bindings today; app includes those entry modules, but the final composition root is not purely app-owned yet.

Current guardrails still enforce the important lower-level boundary:

- `core:*` must not depend on `feature:*`.
- Feature modules must not depend on data, storage, or network implementation modules.
- Feature API modules must not depend on feature implementation or entry modules.
- Feature implementation modules must not depend on other feature implementation or entry modules directly.
- App must not depend on feature implementation modules directly.

## Split Decision

`routine`, `exercise`, `analysis`, and `workout` are valid feature candidates because they map to user-visible destinations or flows. The app-owned coordinator should now stay narrow: route selection, feature composition, and explicit cross-feature handoffs only. The safer path is to keep moving one cohesive destination, dialog, or flow per PR until only app-level routing and composition remain.
