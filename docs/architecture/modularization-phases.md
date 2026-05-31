# Modularization Phases

This plan follows the Android modularization guidance: keep modules highly cohesive, minimize coupling, expose only the API needed by other modules, and let the app module own app startup and root navigation.

## Target Shape

The final architecture should make the app module a thin composition layer:

- `:app` owns application startup, session gating, root navigation, and top-level chrome.
- `:core:*` modules provide reusable models, domain contracts, data implementations, design tokens, and shared UI primitives.
- `:feature:*:api` modules expose stable route keys and public contracts only.
- `:feature:*:impl` modules own screen implementation, ViewModels, and feature-specific UI.
- `:app` owns the DI composition root that binds feature API contracts to feature implementations.

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

## Phase 24: Routine Route API Black-Boxing

Status: stacked after Phase 23 on `codex/modularization-routine-route-blackbox`.

Stop exposing routine screen internals through the public routine API. App may still coordinate cross-feature recording and exercise-detail flows, but it should not construct routine UI state, routine action sets, custom routine form state, or routine dialog/content pieces directly.

First PR scope:

- Replace public routine UI/action handoffs with an opaque `RoutineRouteState`.
- Move routine-only UI, action, recommendation form, custom builder form, and next-day presentation models from `:feature:routine:api` into `:feature:routine:impl`.
- Keep only a narrow `RoutineCoordinatorState` in `:feature:routine:api` for the app coordinator's remaining routine-recording handoff.
- Let routine-owned route state render home summary, routine content, and routine dialogs without app seeing `RoutineUiState` or `RoutineActions`.
- Keep app-owned cross-feature orchestration intact until the next phase moves routine-progress continuation policy out of `TrainingViewModel`.

Next PR scope:

- Move app `TrainingViewModel`'s routine-progress continuation policy out of app so app does not inspect `WeeklyPlan` to choose the next routine exercise after a record save.
- Revisit the shared training shell so feature routes can own full list surfaces instead of providing `LazyListScope` content.
- Split broad core-domain repository contracts before introducing feature-owned data/domain/network modules.

## Phase 25: Routine Continuation Policy Ownership

Status: stacked after Phase 24 on `codex/modularization-routine-continuation-policy`.

Move the routine-specific record continuation and planned-exercise lookup policy behind the routine feature boundary. App remains the route and dialog coordinator, but it no longer inspects `WeeklyPlan`, completed routine ids, or routine builder visibility to decide what happens after a record save or whether exercise detail can launch recording.

First PR scope:

- Remove `RoutineCoordinatorState` from the public routine API.
- Add opaque `RoutineRouteState` methods for routine-owned continuation and recordability decisions.
- Move same-day routine continuation into `:feature:routine:impl`.
- Keep app `TrainingViewModel` as a generic single/continuous recording-flow coordinator.
- Remove stale app-side training localization helpers and `training_*` resources now owned by feature modules.

Split decision:

- Do not add `:feature:routine:domain` or `:feature:routine:data` for this phase. The moved logic is presentation/flow policy over already-loaded routine UI state, not a new repository contract or feature-private data source.
- Keep shared persistence contracts in `:core:domain` and implementations in `:core:data`.

Next PR scope:

- Revisit the shared training shell so feature routes can own full list surfaces instead of providing `LazyListScope` content.
- Split broad core-domain repository contracts before introducing feature-owned data/domain/network modules.
- Continue shrinking app training code to route selection, tab composition, and explicit cross-feature handoffs only.

## Phase 26: Feature-Owned Training Surfaces

Status: stacked after Phase 25 on `codex/modularization-feature-owned-training-surfaces`.

Stop using app-owned `LazyListScope` assembly as the public routine/exercise screen contract. App still owns navigation, route selection, and cross-feature dialog handoffs, but routine and exercise now expose composable route surfaces instead of asking app to provide a list container.

First PR scope:

- Move the shared training screen chrome, background, safe-drawing insets, header, and scrolling surface into `:core:ui`.
- Replace routine API `LazyListScope` fragments with composable `RoutineRouteState` route methods.
- Replace exercise catalog API `rememberUiState` plus `LazyListScope.Content` with a feature-owned composable route.
- Keep app `TrainingRoute` focused on workout-recording, exercise-detail, and routine dialog coordination.
- Move exercise catalog UI state out of the public API and back into `:feature:exercise:impl`.

Split decision:

- Do not add `:feature:*:domain`, `:feature:*:data`, or `:feature:*:network` in this phase. The pressure point is UI ownership, not feature-private repository ownership.
- Keep shared screen chrome in `:core:ui` because multiple feature routes use it and it contains no feature-specific state.
- Keep app passing app-level chrome strings into feature routes; features own the screen body and common surface usage, while app remains the navigation control tower.

Next PR scope:

- Split the broad `TrainingRepository` contract inside `:core:domain` into smaller shared contracts before considering feature-owned domain/data modules.
- Revisit `:feature:*:entry` Hilt bindings and move composition-root bindings into app once the route APIs no longer force coordinator-mediated screen state.
- Continue tightening feature APIs by removing state/action rendering escape hatches that only exist for previews or tests.

## Phase 27: Shared Repository Contract Split

Status: stacked after Phase 26 on `codex/modularization-split-training-repositories`.

Split the broad `TrainingRepository` contract into narrower shared `:core:domain` contracts aligned by data concern. This keeps shared persistence contracts in core while preventing every use case, feature test, and Hilt binding from depending on one catch-all training repository.

First PR scope:

- Replace `TrainingRepository` with `ExerciseRepository`, `RoutinePlanRepository`, `RoutineProgressRepository`, `WorkoutLogRepository`, and `WeeklySummaryRepository`.
- Update use cases so each constructor depends only on the narrow repository capability it needs.
- Keep `DefaultTrainingRepository` as the single `:core:data` implementation for now, but bind it to each narrow core-domain contract.
- Update app android tests and feature unit-test fakes to implement only the contracts used by the subject under test.

Split decision:

- Do not add `:feature:*:domain`, `:feature:*:data`, or `:feature:*:network` here. The contracts are still shared by multiple features: exercise lookup spans routine/exercise/analysis, workout logs span workout/exercise/routine/analysis, and routine plan/progress state is read by the routine feature but still participates in app-wide training flow.
- Do not split `DefaultTrainingRepository` into multiple data implementations in this phase. The current implementation shares active-session resolution, template lookup, custom routine storage, plan building, and weekly summary composition; separating implementation classes should be a follow-up once the contracts prove stable.
- Keep network out of scope. There are no feature-specific remote contracts in this split; DTO/network contracts continue to belong in `:core:network` when they appear.

Next PR scope:

- Move `:feature:*:entry` Hilt feature-entry bindings into the app composition root, with an explicit app-to-feature-impl guardrail exception for DI wiring only.
- Consider splitting `DefaultTrainingRepository` into internal core-data collaborators after the contract split has settled.
- Continue checking whether any future repository is truly feature-private before adding `:feature:*:domain` or `:feature:*:data`.

## Phase 28: App-Owned Feature DI Composition

Status: stacked after Phase 27 on `codex/modularization-app-owned-feature-di`.

Move final feature-entry binding assembly into `:app` so routing and DI composition are both app-owned. Feature APIs remain the contracts consumed by app navigation; feature implementations remain responsible for screen/ViewModel internals.

First PR scope:

- Add an app-owned Hilt module that binds `AnalysisFeatureEntry`, `ExerciseCatalogFeatureEntry`, `ExerciseDetailFeatureEntry`, `RoutineFeatureEntry`, `WorkoutRecordingFeatureEntry`, and `ExerciseMediaRenderer` to their feature implementations.
- Remove the now-redundant `:feature:analysis:entry`, `:feature:exercise:entry`, `:feature:routine:entry`, and `:feature:workout:entry` modules.
- Change app dependencies from feature entry modules to the corresponding feature implementation modules.
- Update module-boundary checks with an explicit `:app` to `:feature:*:impl` allowlist for composition-root DI only.

Split decision:

- Allow direct `:app` to feature implementation dependencies only because `:app` is the composition root. Feature implementation classes should stay referenced from app DI wiring, not from app route bodies or feature modules.
- Keep feature APIs as the routing contracts. App navigation still routes through API interfaces, while Hilt decides which implementation satisfies each contract.
- Do not add `:feature:*:domain`, `:feature:*:data`, or `:feature:*:network` in this phase. This is a DI assembly cleanup, not a repository ownership split.

Next PR scope:

- Consider splitting `DefaultTrainingRepository` into core-data collaborators if the narrower core-domain contracts keep holding.
- Continue auditing app package imports so feature implementation symbols remain isolated to app-owned DI modules.

## Phase 29: Core Data Repository Implementation Split

Status: stacked after Phase 28 on `codex/modularization-core-data-repository-collaborators`.

Split the remaining catch-all `DefaultTrainingRepository` implementation into repository implementations that match the already-split `:core:domain` contracts. Keep all shared persistence contracts in `:core:domain` and all shared implementations in `:core:data`.

First PR scope:

- Replace `DefaultTrainingRepository` with `DefaultExerciseRepository`, `DefaultRoutinePlanRepository`, `DefaultRoutineProgressRepository`, `DefaultWorkoutLogRepository`, and `DefaultWeeklySummaryRepository`.
- Keep one `DataModule`, but bind each core-domain repository contract directly to its matching core-data implementation.
- Add small shared core-data collaborators for active-session resolution and seed/template weekly-plan construction.
- Preserve planned exercise id behavior for system and custom routines with focused unit coverage.

Split decision:

- Do not introduce `:feature:*:domain` or `:feature:*:data` here. These repositories still serve multiple features and app-wide training flows.
- Do not split Room/DataStore contracts. Room remains in `:core:database`; DataStore remains in `:core:datastore`.
- Do not split `TrainingMappers.kt` yet. Mapper file shape is secondary to removing the broad repository implementation.

Next PR scope:

- Re-audit remaining app coordinator responsibilities now that feature routing, feature ViewModels, DI assembly, and shared data implementations are no longer catch-all structures.
- Consider splitting `TrainingMappers.kt` by storage concern only if mapper growth starts obscuring ownership.

## Phase 30: App-Owned Core Repository DI Assembly

Status: stacked after Phase 29 on `codex/modularization-app-owned-core-data-di`.

Move shared repository binding assembly out of `:core:data` and into the app composition root. Repository interfaces remain in `:core:domain`; implementations remain in `:core:data`; the app now owns the final choice of which implementation satisfies each shared contract.

First PR scope:

- Replace the core-data `DataModule` with app-owned `CoreRepositoryBindingsModule`.
- Keep repository implementation classes in `:core:data` and bind them from `app/src/main/java/com/smarttrainner/app/di`.
- Update Hilt UI tests to uninstall the app-owned repository binding module before binding in-memory repository fakes.
- Extend `checkModuleBoundaries` so app production code may reference `core:data`, `core:database`, `core:datastore`, or `core:network` only from app-owned DI composition modules.

Split decision:

- Do not move database, datastore, or network provider modules in this phase. Those modules still own platform/resource construction for their implementation details; this phase targets app-level repository contract assembly.
- Do not add `:feature:*:domain`, `:feature:*:data`, or `:feature:*:network`. The repository contracts remain shared across multiple feature flows.

Next PR scope:

- Re-audit the remaining app `TrainingViewModel` and coordinator routes to confirm they hold only cross-feature handoff state.
- Consider whether `TrainingMappers.kt` should be split by storage concern after DI assembly no longer hides repository ownership.

## Phase 31: App-Owned Platform Provider DI Assembly

Status: stacked after Phase 30 on `codex/modularization-app-owned-platform-di`.

Move the remaining production Hilt provider modules out of core implementation modules and into the app composition root. Core modules still own their contracts and implementation classes: Room database/DAO contracts stay in `:core:database`, DataStore access stays in `:core:datastore`, and Retrofit DTO/API contracts stay in `:core:network`. The app now owns the final provider assembly for those platform resources.

First PR scope:

- Move database, network, and clock provider modules to `app/src/main/java/com/smarttrainner/app/di`.
- Remove Hilt Gradle plugin/compiler usage from core modules that no longer declare production Hilt modules.
- Extend `checkModuleBoundaries` so production `@Module`/`@InstallIn` declarations are allowed only in app-owned DI composition modules.
- Extend `checkModuleBoundaries` so new `:feature:*:domain`, `:feature:*:data`, or `:feature:*:network` modules require an explicit ownership decision before they can be introduced.

Split decision:

- Do not move Room entities, DAOs, migrations, DataStore data sources, Retrofit API contracts, or DTOs into app. App owns wiring; core modules still own implementation contracts and storage/network types.
- Do not add `:feature:*:network`, `:feature:*:data`, or `:feature:*:domain` here. The provider wiring is app-level composition, and the currently wired repositories remain shared core contracts.

Next PR scope:

- Tighten remaining feature API route contracts beyond analysis, especially feature APIs that expose helper/rendering surfaces not consumed by app routing.
- Continue auditing test fakes and fixtures so app tests depend on domain contracts first and implementation fixtures only when intentionally exercising production content.

## Phase 32: Analysis Route-Only API Contract

Status: stacked after Phase 31 on `codex/modularization-analysis-route-contract`.

Tighten the analysis feature API so it exposes only the app-routable feature entry. The app already delegates the analysis destination through `AnalysisFeatureEntry.Route()`, so the content-rendering API and UI state models are implementation details rather than cross-module contracts.

First PR scope:

- Remove `AnalysisFeatureEntry.Content(state)` from `:feature:analysis:api`.
- Move analysis UI state models into `:feature:analysis:impl`.
- Drop `:core:model` and core library desugaring from the analysis API module once the public API no longer exposes domain/UI state models.

Split decision:

- Do not introduce `:feature:analysis:domain` or `:feature:analysis:data`; analysis still consumes shared core-domain use cases and has no feature-private repository contract.
- Keep the app's responsibility limited to routing and DI composition; analysis view model state remains feature-owned.

Next PR scope:

- Continue tightening public feature APIs that still expose helper surfaces beyond app-owned routing needs.
- Re-audit whether exercise media rendering should remain as a shared core UI contract or be separated from `ExerciseFeatureEntryImpl` ownership.

## Phase 33: Workout Route-Only API Contract

Status: stacked after Phase 32 on `codex/modularization-workout-route-contract`.

Tighten the workout recording feature API so app-owned training coordination can launch the record dialog without seeing the workout form state, validation errors, or rendering actions. The workout feature already owns the ViewModel and dialog implementation; the public API should expose only the route entry needed by app routing.

First PR scope:

- Remove the public `WorkoutRecordingFeatureEntry.Dialog(state, actions)` content-rendering escape hatch.
- Move workout recording UI state, form state, form errors, and action models into `:feature:workout:impl`.
- Keep `WorkoutRecordingFeatureEntry.DialogRoute(...)` as the app-facing contract because app still owns the cross-feature handoff from routine/exercise selection into the recording flow.
- Keep `:feature:workout:api` depending on `:core:model` and `:core:ui` because its route contract still accepts `PlannedExercise`, `ExerciseId`, and the shared `ExerciseMediaRenderer`.

Split decision:

- Do not introduce `:feature:workout:domain` or `:feature:workout:data`; workout recording still saves through shared core-domain workout log use cases and has no feature-private repository contract.
- Keep form validation and prefill policy in `:feature:workout:impl`; they are feature-owned presentation/application logic, not shared domain contracts.

Next PR scope:

- Apply the same route-only tightening to exercise detail if its public `Dialog(state, actions)` surface remains unused by app routing.
- Re-audit the shared exercise media renderer contract once exercise detail no longer exposes UI state models from its API.

## Phase 34: Exercise Detail Route-Only API Contract

Status: stacked after Phase 33 on `codex/modularization-exercise-detail-route-contract`.

Tighten the exercise detail feature API so app routing can open the detail dialog without seeing exercise detail UI state or rendering actions. The app already uses `ExerciseDetailFeatureEntry.DialogRoute(...)`; the lower-level dialog rendering surface is an implementation detail.

First PR scope:

- Remove the public `ExerciseDetailFeatureEntry.Dialog(state, actions)` content-rendering escape hatch.
- Move exercise detail UI state and actions into `:feature:exercise:impl`.
- Drop unused Compose foundation/UI API dependencies from `:feature:exercise:api`; the public API needs Compose runtime, `core:model` route ids, and `core:ui` screen chrome only.

Split decision:

- Do not introduce `:feature:exercise:domain` or `:feature:exercise:data`; exercise catalog/detail still consume shared exercise and workout-log core-domain use cases.
- Keep `ExerciseMediaRenderer` in `:core:ui` for now because routine/workout screens need a feature-neutral rendering port without depending on `:feature:exercise:*`.

Next PR scope:

- Re-audit `RoutineRouteState`, which still exposes a broad routine facade to app training coordination.
- Decide whether `ExerciseMediaRenderer` should stay in `:core:ui` or move to a more explicit common training UI taxonomy.

## Phase 35: Routine Entry-Owned Route Surfaces

Status: stacked after Phase 34 on `codex/modularization-routine-entry-routes`.

Tighten the routine feature API so app routing calls routine screen/dialog surfaces through `RoutineFeatureEntry`, not through a broad state object. `RoutineRouteState` remains only as the opaque handle for cross-feature coordination decisions the app still needs while composing the training flow.

First PR scope:

- Move `HomeSummaryRoute`, `Route`, and `Dialogs` off `RoutineRouteState` and onto `RoutineFeatureEntry`.
- Keep routine UI state and actions inside `:feature:routine:impl`; app never receives raw routine UI/action/form models.
- Keep `RoutineRouteState` limited to current routine chrome text plus routine-owned continuation and recordability policy needed by workout/exercise handoffs.
- Update app training routes so routine destinations and dialogs are rendered by the routine feature entry.

Split decision:

- Do not add `:feature:routine:domain` or `:feature:routine:data` in this route-surface PR. This phase narrows the public route contract; it does not move repository ownership.
- A feature-private routine domain module is now the first concrete candidate. Routine-only policy/use cases such as recommendation, routine day advancement, and cycle-completion resolution can move to `:feature:routine:domain` once that module is explicitly approved.
- Keep shared read repository contracts in `:core:domain` when multiple flows or summaries depend on them. `RoutinePlanRepository.observeCurrentWeeklyPlan` remains shared because weekly summary/analysis data is derived from it.
- Do not add `:feature:routine:data` before splitting shared read contracts from routine-only command contracts. Feature data must not depend on `:core:data` implementations; final DI assembly should still happen in `:app`.

Next PR scope:

- Introduce `:feature:routine:domain` for routine-only pure policy/use cases, with an explicit guardrail allowlist.
- Then split routine repository contracts into shared core reads and routine-private commands before considering `:feature:routine:data`.
- Keep checking that feature modules do not know about one another and that `:app` remains the routing and DI composition root.

## Phase 36: Routine Feature Domain Policies

Status: stacked after Phase 35 on `codex/modularization-routine-domain-policies`.

Introduce the first feature-local domain module for routine-only policy. This follows the ownership rule that shared repository contracts stay in `:core:domain`, while logic used only by one feature can live under that feature.

First PR scope:

- Add `:feature:routine:domain` as an explicitly approved feature-local domain module.
- Move `RecommendRoutineUseCase`, `ResolveRoutineCycleCompletionUseCase`, and `EvaluateRoutineReadinessUseCase` from `:core:domain` to `:feature:routine:domain`.
- Point `:feature:routine:impl` at the new routine domain module for those policies.
- Move the policy unit tests with the moved policies; keep custom routine validation and routine completion command tests in `:core:domain`.

Split decision:

- Keep `AdvanceRoutineDayUseCase` and `CompleteRoutineDayUseCase` in `:core:domain` for this PR because `CompleteRoutineDayUseCase` still depends on the shared `RoutineProgressRepository` command contract. Moving only `AdvanceRoutineDayUseCase` would either make core depend on a feature or duplicate the policy.
- Do not add `:feature:routine:data` yet. The next repository step should first split shared core read contracts from routine-only command contracts.
- Keep weekly plan/template reads in core while analysis/summary flows derive shared data from them.

Next PR scope:

- Split routine command contracts, especially current routine progress/custom routine commands, away from shared plan/template reads.
- After that split, evaluate `:feature:routine:data` with app-owned DI assembly as the only place that sees the implementation.
- Revisit whether `CompleteRoutineDayUseCase`, `StartRoutineUseCase`, custom routine save/delete, and routine progress repository commands should move together.

## Phase 37: Routine Command Domain And Data Ownership

Status: stacked after Phase 36 on `codex/modularization-routine-command-data`.

Split routine repository ownership by purpose. Shared routine reads remain in `:core:domain` because weekly plans, summaries, and multiple feature flows consume them. Routine-only commands now belong to the routine feature contract, and the Room/DataStore-backed implementation lives in routine feature data.

First PR scope:

- Add `:feature:routine:data` as an explicitly approved feature-local data module.
- Move routine command contracts to `:feature:routine:domain`: `RoutinePlanCommandRepository` and `RoutineProgressCommandRepository`.
- Move routine command use cases to `:feature:routine:domain`: custom routine validation/save/delete/select plus routine start/advance/complete.
- Move `DefaultRoutinePlanRepository` and `DefaultRoutineProgressRepository` to `:feature:routine:data`; each implementation binds to both shared core read contracts and routine command contracts.
- Move routine custom-routine database mappers into `:feature:routine:data`.
- Move `SeedTrainingContent` and `TrainingSeedStore` to `:core:domain` because seed exercises/templates are shared domain catalog content used by shared reads and routine data.
- Move `ActiveSessionResolver` to `:core:datastore` so core workout data and routine feature data can share session resolution without a `feature -> core:data` dependency.
- Keep final production DI assembly in `:app`, where routine data implementations are bound to both core read interfaces and feature command interfaces.

Split decision:

- Do not allow `:feature:routine:data -> :core:data`. Feature data may use only explicitly approved core infrastructure contracts (`:core:database`, `:core:datastore`) plus shared model/domain modules.
- Do not make `:feature:routine:impl` depend on `:feature:routine:data`; the UI implementation depends on domain use cases/contracts only.
- Keep read-only `RoutinePlanRepository` and `RoutineProgressRepository` in `:core:domain` for now because they still feed shared weekly plan/progress surfaces.

Next PR scope:

- Re-audit whether routine progress and plan-template catalog reads are truly shared or can also become routine feature-owned.
- Apply the same feature-domain/data ownership test to workout commands if workout log persistence ever becomes feature-private rather than shared analysis/routine input.
- Continue tightening app so it sees feature data only inside app-owned DI modules.

## Phase 38: Routine Read Contract Ownership

Status: stacked after Phase 37 on `codex/modularization-routine-read-contracts`.

Finish the routine repository ownership split by moving routine-only read contracts into the routine feature. Shared weekly-plan reads stay in core because weekly summaries and analysis derive app-wide metrics from them.

First PR scope:

- Replace the remaining core `RoutinePlanRepository` read surface with `WeeklyPlanRepository`, which exposes only `observeCurrentWeeklyPlan`.
- Move routine template catalog reads to `:feature:routine:domain` as `RoutinePlanCatalogRepository` and `ObservePlanTemplatesUseCase`.
- Move active routine progress reads to `:feature:routine:domain` as `RoutineProgressRepository` and `ObserveRoutineProgressUseCase`.
- Keep `DefaultRoutinePlanRepository` and `DefaultRoutineProgressRepository` in `:feature:routine:data`; app-owned DI binds the plan repository to both the shared core weekly-plan contract and routine-owned catalog/command contracts.
- Keep custom-routine listing private inside routine data; no production feature consumes a separate `observeCustomRoutines` contract.

Split decision:

- `observeCurrentWeeklyPlan` remains shared core domain because analysis summary data derives weekly metrics from it.
- `observePlanTemplates` and `observeRoutineProgress` are routine feature concerns today; analysis, exercise, and workout do not depend on those repository contracts.
- Do not introduce `:feature:routine:network`. Routine data uses core storage infrastructure only; network contracts still belong in `:core:network` if a remote training catalog appears.

Next PR scope:

- Apply the same ownership test to workout log commands only if a command becomes feature-private. Current workout logs remain shared analysis/routine/exercise input.
- Continue reducing app imports so feature data classes appear only in app-owned DI composition modules.

## Phase 39: App DI Routine Data Assembly Split

Status: stacked after Phase 38 on `codex/modularization-app-di-routine-data-split`.

Keep `:app` as the final DI composition root, but separate core shared repository assembly from routine feature data assembly. A module named for core repositories should not also bind routine feature-private data implementations.

First PR scope:

- Keep `CoreRepositoryBindingsModule` focused on shared core-data repository implementations.
- Add an app-owned `RoutineDataRepositoryBindingsModule` that binds `DefaultRoutinePlanRepository` and `DefaultRoutineProgressRepository` to the shared `WeeklyPlanRepository` contract and routine-owned read/command contracts.
- Update Hilt UI tests to uninstall both core and routine repository binding modules before installing in-memory replacements.
- Tighten `checkModuleBoundaries` so app feature-data imports are allowed only in approved feature-data repository binding modules, not every app DI file.

Split decision:

- Do not move DI out of `:app`. The app remains the control tower for selecting concrete implementations.
- Do not create a generic feature-data composition abstraction yet. Only `:feature:routine:data` exists, so an explicit routine binding module is clearer than a speculative framework.
- Keep `WeeklyPlanRepository` bound from routine data in app DI because the current weekly plan implementation is backed by routine plan storage while the contract remains shared.

Next PR scope:

- Audit whether workout log write commands are still shared app capabilities or should split into workout-owned command contracts.
- Continue tightening app DI file-level guardrails as more feature-local data modules are introduced.

## Phase 40: App Training Flow State Coordinator

Status: stacked after Phase 39 on `codex/modularization-training-flow-state-coordinator`.

Keep navigation and cross-feature orchestration in `:app`, but make the app-owned training flow state explicit and testable. The current training flow coordinates routine callbacks, exercise detail, and workout recording dialogs with shared core models; it is not owned by one feature and should not become a feature-local domain/data module.

First PR scope:

- Add an app-local `TrainingFlowState` reducer for selected exercise, recording dialog target, and single-vs-continuous recording mode.
- Keep `TrainingViewModel` as the lifecycle-aware shell that exposes `TrainingUiState` and delegates state transitions.
- Scope `TrainingViewModel` to the activity-level app coordinator instead of per destination, so home, routine, and exercise tabs share the same cross-feature flow.
- Add pure state tests for single recording, continuous recording advancement, and continuous flow completion.

Split decision:

- Do not add a new `feature:*:domain`, `feature:*:data`, or `feature:*:network` module for this flow. The state is app routing/coordinator state, not a feature-owned contract or implementation.
- Do not move this state to `core:*` yet. It is not reused by lower layers and references a concrete app navigation workflow.
- Continue using `:app` as the only module that wires routine, exercise, and workout feature APIs together.

## Phase 41: Routine Common UI Deduplication

Status: stacked after Phase 40 on `codex/modularization-routine-common-ui-dedupe`.

Keep common Compose primitives in `:core:ui` instead of duplicating them inside a feature implementation. Routine still owns routine-specific strings and status semantics, but common badge and empty-state visuals should remain shared.

First PR scope:

- Replace routine-local `TrainingBadge`, `TrainingBadgeRow`, and `TrainingBadgeSpec` with `SmartTrainnerBadge`, `SmartTrainnerBadgeRow`, and `SmartTrainnerBadgeSpec` from `:core:ui`.
- Replace the routine-local `EmptyState` wrapper with direct `SmartTrainnerEmptyState` usage.
- Keep only the routine-specific `StatusIcon` wrapper because it supplies localized routine completion content descriptions.
- Keep `SmartTrainnerBadgeRow` implemented with stable common layout primitives so shared UI usage does not depend on experimental flow-layout runtime signatures.
- Document that no new feature-local repository, domain, data, or network module is needed for this UI ownership cleanup.

Split decision:

- Do not add `:feature:routine:domain` or `:feature:routine:data` for this phase. The change removes duplicated UI composition, not a feature-private repository contract or data implementation.
- Do not add `:feature:routine:network`. Routine still has no feature-specific remote contract; shared network contracts belong in `:core:network` when needed.
- Keep common, domain-free UI primitives in `:core:ui`. Feature-specific copy, state, events, and policy remain in the feature implementation/domain modules that own them.

Next PR scope:

- Continue auditing feature implementations for duplicated common UI wrappers before adding new modules.
- Re-audit workout log commands only if a command surface becomes feature-private rather than shared analysis/routine/exercise input.
- Keep app as the final routing and DI composition root while feature modules remain isolated from each other.

## Phase 42: Routine API Dependency Trim

Status: stacked after Phase 41 on `codex/modularization-routine-api-dependency-trim`.

Keep feature API modules as contract-only surfaces. The routine API still needs Compose runtime for its route entry, `:core:model` for app handoff models, and `:core:ui` for shared route chrome/media contracts, but it should not leak unused implementation-facing dependencies.

First PR scope:

- Remove the unused Compose foundation API dependency from `:feature:routine:api`.
- Remove core library desugaring from `:feature:routine:api` because the public routine contract no longer exposes `java.time` or other desugared JDK APIs.
- Keep `:core:model` and `:core:ui` in the public API only while app/routine/workout handoffs still pass `ExerciseId`, `PlannedExercise`, `SmartTrainnerScreenChrome`, and `ExerciseMediaRenderer`.

Split decision:

- Do not add a new feature domain/data/network module for this cleanup. The pressure point is public API dependency hygiene, not repository ownership.
- Keep the next repository ownership audit focused on workout log reads/writes, which remain shared until proven feature-private.

Next PR scope:

- Continue trimming public feature API action wrappers where a direct callback would preserve the route contract with less exposed surface.
- Re-audit whether `ExerciseMediaRenderer` belongs permanently in `:core:ui` or should move to a narrower common training presentation contract.

## Phase 43: Feature Media Renderer DI Ownership

Status: stacked after Phase 42 on `codex/modularization-feature-media-renderer-di`.

Keep `ExerciseMediaRenderer` as a common UI presentation contract for now, but do not expose it through routine/workout route APIs. The app should choose the production renderer through DI composition; route callers should not manually thread that renderer across app navigation.

First PR scope:

- Inject `ExerciseMediaRenderer` into routine and workout feature entry implementations through app-owned Hilt composition.
- Remove the renderer parameter from `RoutineFeatureEntry.Route` and `WorkoutRecordingFeatureEntry.DialogRoute`.
- Stop passing the renderer through `MainActivity`, `SmartTrainnerApp`, app navigation, and app training coordinator routes.
- Remove `:core:ui` from `:feature:workout:api` because the workout public contract no longer references common UI types.

Split decision:

- Do not add `:feature:exercise:domain`, `:feature:exercise:data`, or `:feature:workout:data` for this phase. The renderer is presentation composition, not a repository contract or data implementation.
- Keep the renderer contract in `:core:ui` while both routine and workout UI use it and the concrete exercise implementation provides it through app DI.
- Keep final DI assembly in `:app`; feature implementations request the contract, and app DI selects the exercise implementation as the renderer.

Next PR scope:

- Audit `SmartTrainnerScreenChrome` in routine/exercise APIs as the next remaining common UI type exposed through public feature route contracts.
- Revisit workout command ownership separately; workout recording writes may justify `:feature:workout:domain` and `:feature:workout:data`, but this UI composition cleanup does not.

## Phase 44: Feature Route Chrome Contract Trim

Status: stacked after Phase 43 on `codex/modularization-feature-route-chrome-contract`.

Keep app-owned routing responsible for the route title/subtitle values, but do not expose the shared `SmartTrainnerScreenChrome` UI type through feature APIs. Feature implementations can still render the shared scaffold internally because that is implementation UI composition, not a public contract.

First PR scope:

- Replace `SmartTrainnerScreenChrome` parameters in exercise and routine public route APIs with `title` and `subtitle` strings.
- Construct `SmartTrainnerScreenChrome` inside exercise/routine implementations before calling `SmartTrainnerScreenScaffold`.
- Keep app training routes as the control tower for selecting route title/subtitle values.
- Remove `:core:ui` from `:feature:exercise:api` and `:feature:routine:api`.

Split decision:

- Do not add a new shared route chrome model in `:core:model`. The contract is currently only two display strings and does not need a domain or model type.
- Do not move the shared scaffold out of `:core:ui`; it is still common UI reused by feature implementations.
- Do not add feature domain/data/network modules for this phase because the change is public UI-contract hygiene, not repository ownership.

Next PR scope:

- Continue trimming public feature API wrappers, especially single-callback action holder types.
- Re-audit workout recording repository commands separately for possible `:feature:workout:domain` and `:feature:workout:data` ownership.

## Phase 45: Exercise Catalog Callback Contract

Status: stacked after Phase 44 on `codex/modularization-exercise-catalog-callback-contract`.

Keep public feature APIs as small as their current contract requires. The exercise catalog route has one app-owned handoff callback, so it does not need a public action-holder model.

First PR scope:

- Remove the public `ExerciseCatalogActions` wrapper from `:feature:exercise:api`.
- Pass `onExerciseSelected` directly through `ExerciseCatalogFeatureEntry.Route`.
- Keep the selection callback app-owned because it drives the app training coordinator's selected exercise dialog state.
- Update exercise implementation internals to pass the callback through content rows without introducing a replacement wrapper.

Split decision:

- Do not add a feature domain/data/network module for this phase. The change only trims an API callback wrapper.
- Keep `ExerciseId` in the public exercise API because app routing still coordinates selected exercise handoffs.
- Do not introduce a generic route action abstraction until a route has multiple callbacks that justify a named contract.

Next PR scope:

- Re-audit workout recording command ownership for a possible workout-owned domain/data split.
- Continue checking whether app-owned training coordination can expose fewer core model handoffs.

## Phase 46: Workout Recording Command Data Ownership

Status: stacked after Phase 45 on `codex/modularization-workout-command-data-split`.

Split workout log ownership by use. Workout log reads remain in `:core:domain` because analysis, exercise, routine, workout, and weekly summary flows consume them. Workout recording commands now belong to the workout feature because production save/prefill command use is owned by the workout recording dialog.

First PR scope:

- Keep `WorkoutLogRepository` in `:core:domain` as a shared read contract with `observeWorkoutLogs` and `observeLatestWorkoutLogs`.
- Move recording command use cases to `:feature:workout:domain`: `GetLatestWorkoutLogUseCase` and `SaveWorkoutLogUseCase`.
- Add `WorkoutRecordingRepository` in `:feature:workout:domain` for command-only persistence.
- Add `:feature:workout:data` with `DefaultWorkoutRecordingRepository`, backed by `core:database` and `core:datastore` infrastructure.
- Keep final production DI assembly in `:app` through `WorkoutDataRepositoryBindingsModule`.

Split decision:

- Do not move workout log read contracts into workout feature modules; those reads are shared inputs to analysis, exercise detail/catalog, routine state, weekly summary, and workout recording.
- Do not make `:feature:workout:impl` depend on `:feature:workout:data`; the UI implementation consumes domain use cases only.
- Do not introduce `:feature:workout:network`; workout recording uses local storage infrastructure only.
- Allow `:feature:workout:data` to depend on `:core:database` and `:core:datastore`, but not `:core:data`.

Next PR scope:

- Re-check whether any remaining app handoffs still expose more core model detail than routing requires.
- Continue keeping feature-data bindings in explicit app-owned DI modules as additional feature data modules are introduced.

## Phase 47: Analysis Summary Data Ownership

Status: stacked after Phase 46 on `codex/modularization-analysis-summary-data-split`.

Split analysis weekly summary ownership by purpose. Weekly plan and workout log reads remain shared `:core:domain` contracts because multiple flows consume those raw app data streams. The weekly summary projection, calculator, and use case are analysis-owned because production consumption is limited to the analysis feature.

First PR scope:

- Move `WeeklySummaryRepository`, `ObserveWeeklySummaryUseCase`, and `WeeklySummaryCalculator` to `:feature:analysis:domain`.
- Add `:feature:analysis:data` with `DefaultWeeklySummaryRepository`, which composes the shared core `WeeklyPlanRepository` and `WorkoutLogRepository` reads.
- Keep `WeeklyPlanRepository` and `WorkoutLogRepository` in `:core:domain` as shared read contracts.
- Bind the analysis summary implementation from `:app` through `AnalysisDataRepositoryBindingsModule`.
- Update app UI test fakes and analysis ViewModel tests to depend on the analysis-owned summary contract.

Split decision:

- Do not move weekly plan or workout log reads into analysis; those are shared inputs used outside analysis.
- Do not make `:feature:analysis:impl` depend on `:feature:analysis:data`; the UI implementation consumes the analysis domain use case only.
- Do not introduce `:feature:analysis:network`; summary data is derived from local/shared core read contracts.
- Do not allow `:feature:analysis:data` to depend on `:core:data`; it composes core-domain read contracts and owns only the analysis projection implementation.

Next PR scope:

- Continue checking whether remaining shared core-domain contracts are truly shared or are feature-private commands/projections.
- Keep app-owned DI modules explicit per feature data area instead of introducing a generic composition abstraction.

## Phase 48: Routine Weekly Plan Use Case Ownership

Status: stacked after Phase 47 on `codex/modularization-routine-weekly-plan-usecase`.

Keep the shared weekly-plan read contract in `:core:domain`, but move the routine-only use-case wrapper into `:feature:routine:domain`. The repository contract is shared by routine route state and analysis summary data, while `ObserveCurrentWeeklyPlanUseCase` is injected only by the routine ViewModel.

First PR scope:

- Move `ObserveCurrentWeeklyPlanUseCase` from `:core:domain` to `:feature:routine:domain`.
- Keep `WeeklyPlanRepository` in `:core:domain` because analysis data still composes it for weekly summary projection.
- Point routine ViewModel and tests at the routine-domain use case.
- Avoid adding new feature data or network modules; this is a use-case ownership trim only.

Split decision:

- Do not move `WeeklyPlanRepository` into routine domain while analysis data consumes that shared read contract.
- Do not introduce `:feature:routine:network`; routine weekly plan data still uses local shared storage infrastructure.
- Do not move workout-log read use cases in this PR because their production consumers span routine, workout, exercise, and analysis.

Next PR scope:

- Review unused app-shell/session use cases such as `SignOutUseCase`.
- Review whether unused core network wiring should stay as future infrastructure or be removed until a feature consumes it.

## Phase 49: Session Contract Trim

Status: stacked after Phase 48 on `codex/modularization-session-contract-trim`.

Keep `SessionRepository` in `:core:domain` because active session state gates the app shell before any feature route is shown. Trim the unused sign-out command from the shared contract until a real sign-out/auth feature exists.

First PR scope:

- Remove `SignOutUseCase` from `:core:domain`.
- Remove `SessionRepository.signOut()` from the shared session contract and implementations.
- Remove the unused DataStore `clearActiveSession()` helper that only existed for the removed command.
- Keep `ObserveActiveSessionUseCase` and `StartDefaultSessionUseCase` in core because app startup consumes them directly.

Split decision:

- Do not create `:feature:session:*` or `:feature:auth:*` modules yet; there is no implemented auth/sign-out destination or feature-owned workflow.
- Do not move the session repository into a feature module because session state is app-shell coordination, not a feature-private concern.
- If sign-out returns as a user-facing workflow, introduce it through the app/auth ownership boundary instead of keeping a speculative core-domain command.

Next PR scope:

- Review whether `:core:network` should remain wired in app DI while no repository consumes `SmartTrainnerApi`.

## Phase 50: Drop Unused Network Wiring

Status: stacked after Phase 49 on `codex/modularization-drop-unused-network-wiring`.

Keep `:core:network` as the place where shared remote API contracts and DTOs belong, but remove app-level Retrofit assembly until a repository actually consumes `SmartTrainnerApi`. App-owned DI should be the final composition root for platform providers, not a home for unused future wiring.

First PR scope:

- Remove `PlatformNetworkModule` from app DI.
- Remove the app dependency on `:core:network`.
- Remove app-only JSON/OkHttp/Retrofit dependencies that were used only by the deleted provider module.
- Tighten `checkModuleBoundaries` so app DI no longer has an approved core-network provider file.
- Split the app source guardrail so any `com.smarttrainner.core.network` reference in `:app` fails until network wiring is explicitly reapproved.

Split decision:

- Do not create any `:feature:*:network` module. No feature has a feature-private remote contract yet.
- Do not delete `:core:network` in this PR; it remains the approved location for shared network contracts when a data implementation actually consumes them.
- Reintroduce network provider assembly in app only alongside a real data repository consumer and an explicit boundary decision.

Next PR scope:

- Continue reviewing remaining core-domain read use cases to ensure they are genuinely shared.

## Phase 51: Exercise Detail Use Case Ownership

Status: stacked after Phase 50 on `codex/modularization-exercise-detail-usecase`.

Keep `ExerciseRepository` and shared exercise list reads in `:core:domain`, because exercise catalog, routine planning, and analysis consume the shared exercise catalog. Move the exercise-detail-only lookup use case into `:feature:exercise:domain`.

First PR scope:

- Add `:feature:exercise:domain` as an approved feature-local domain module.
- Move `GetExerciseUseCase` from `:core:domain` to `:feature:exercise:domain`.
- Point exercise detail ViewModel and tests at the exercise-domain use case.
- Keep `ObserveExercisesUseCase` in `:core:domain` because it is consumed by exercise, routine, and analysis.

Split decision:

- Do not create `:feature:exercise:data`; the repository contract and implementation remain shared core catalog infrastructure.
- Do not create `:feature:exercise:network`; there is no exercise-private remote contract.
- Do not move `ExerciseRepository` until exercise catalog data is no longer shared across multiple features.

Next PR scope:

- Tighten app-side guardrails so feature-domain contracts are visible to app only where app-owned DI composes feature data implementations.

## Phase 52: App Feature Domain Boundary Guard

Status: stacked after Phase 51 on `codex/modularization-app-feature-domain-guard`.

Keep `:app` as the final DI composition root while preventing feature-domain contracts from becoming app routing or app shell dependencies. App may know feature APIs for routing and may know feature implementations/data/domain only in approved DI composition modules.

First PR scope:

- Add an explicit app-to-feature-domain Gradle dependency allowlist for the feature-domain modules app needs only for repository binding assembly.
- Tighten app source scanning so `com.smarttrainner.feature.*.domain` imports are allowed only from approved feature-data repository binding modules.
- Keep app routing and app shell code dependent on feature APIs, not feature domain contracts.

Split decision:

- Do not move shared workout-log, exercise catalog, or session contracts in this phase; current production consumers still cross feature/app boundaries.
- Do not add `:feature:*:network`; no feature-private remote contract exists.
- Do not add `:feature:exercise:data`; exercise catalog storage remains shared core infrastructure.

Next PR scope:

- Continue checking shared workout-log/session contracts for actual cross-feature consumers.

## Strict Feature Isolation Audit

Current state is strict at the feature-module dependency level. State ownership is now feature-owned for the major destination and dialog surfaces, with app keeping only cross-feature coordination:

- `:feature:analysis`, `:feature:exercise`, `:feature:routine`, and `:feature:workout` no longer depend on another feature's API, implementation, or entry module.
- `:app` knows feature APIs for routing and listed feature implementations/data/domain only for app-owned DI composition.
- App `TrainingViewModel` now coordinates selected exercise id and a generic single/continuous recording flow; workout recording, exercise detail, exercise catalog, analysis, routine, custom routine builder state, and routine continuation policy are feature-owned.
- App no longer imports routine UI/action/form models or raw routine coordinator state; it consumes only the routine feature entry and opaque route API.
- App no longer owns the routine/exercise `LazyListScope` screen assembly; shared screen chrome lives in `:core:ui`, and feature APIs expose composable route surfaces.
- Core domain persistence contracts are no longer one broad `TrainingRepository`; use cases depend on concern-specific shared contracts.
- Core data repository implementations now mirror those concern-specific contracts instead of one catch-all implementation.
- App-owned DI composition now binds shared core-domain repository contracts to their core-data implementations.
- App-owned DI composition now owns production platform providers for Room and app-wide time; unused Retrofit wiring has been removed until a real repository consumes `SmartTrainnerApi`.
- Analysis API now exposes only a route entry; its content-rendering surface and UI state models are implementation details.
- Analysis weekly summary projection contracts, use case, and calculator now live in `:feature:analysis:domain`.
- Analysis summary projection implementation now lives in `:feature:analysis:data`; app-owned DI binds it to the analysis-owned summary contract.
- App-shell session startup remains a shared core contract; unused sign-out command surface has been removed until a real auth/sign-out workflow exists.
- Exercise-detail-only lookup use case now lives in `:feature:exercise:domain`, while the shared exercise repository and exercise-list use case remain in `:core:domain`.
- Workout recording API now exposes only its dialog route entry; its form state, validation errors, and rendering actions are implementation details.
- Exercise detail API now exposes only its dialog route entry; its detail UI state and rendering actions are implementation details.
- Routine route and dialog rendering now goes through `RoutineFeatureEntry`; `RoutineRouteState` is no longer a public rendering facade.
- Routine-only recommendation, readiness, and cycle-completion policies now live in `:feature:routine:domain`.
- Routine-only command contracts and command use cases now live in `:feature:routine:domain`.
- Routine-only read contracts for template catalog and active progress now live in `:feature:routine:domain`.
- The routine-only weekly-plan use-case wrapper now lives in `:feature:routine:domain`, while the shared weekly-plan repository contract remains in `:core:domain`.
- Routine repository implementations now live in `:feature:routine:data`; app-owned DI binds them to the shared core weekly-plan contract and routine-owned read/command contracts.
- Workout log shared reads remain in `:core:domain`; workout recording commands now live in `:feature:workout:domain`.
- Workout recording persistence now lives in `:feature:workout:data`; app-owned DI binds it to the workout-owned command contract.
- App-owned DI now separates shared core repository bindings from analysis/routine/workout feature-data repository bindings.
- App training flow state now lives in an app-local reducer so `TrainingViewModel` is a thin coordinator shell, while cross-feature routing remains app-owned.
- Routine common badge and empty-state UI now use `:core:ui`; only routine-specific content-description wrapping remains in `:feature:routine:impl`.
- Routine API no longer exports unused Compose foundation or desugaring dependencies; its public dependencies are limited to the contracts still referenced by app routing and handoffs.
- Routine and workout APIs no longer expose `ExerciseMediaRenderer`; app DI provides the renderer to feature implementations instead of app navigation manually threading a common UI renderer.
- Workout API no longer depends on `:core:ui` after removing the media renderer from its public route contract.
- Exercise and routine APIs no longer expose `SmartTrainnerScreenChrome`; app passes only route title/subtitle strings, and feature implementations build common UI chrome internally.
- Exercise, routine, and workout API modules no longer depend on `:core:ui`; common UI is an implementation dependency where needed.
- Exercise catalog API no longer exposes a single-callback action wrapper; app passes `onExerciseSelected` directly as the route handoff.
- Core routine plan reads have narrowed to `WeeklyPlanRepository.observeCurrentWeeklyPlan`, which remains shared because weekly summary and analysis derive from it.
- `:feature:*:entry` modules have been removed; Hilt feature-entry bindings now live in the app composition root.

Current guardrails still enforce the important lower-level boundary:

- `core:*` must not depend on `feature:*`.
- Feature modules must not depend on shared `:core:data` implementations.
- Feature data modules may use core storage/network infrastructure only through explicit allowlists; `:feature:routine:data` and `:feature:workout:data` currently may use `:core:database` and `:core:datastore`, while `:feature:analysis:data` currently uses only shared core domain/model contracts.
- New feature-local domain/data/network modules require an explicit ownership decision before being introduced; `:feature:analysis:domain`, `:feature:analysis:data`, `:feature:exercise:domain`, `:feature:routine:domain`, `:feature:routine:data`, `:feature:workout:domain`, and `:feature:workout:data` are the current approved feature-local modules.
- Feature API modules must not depend on feature implementation or entry modules.
- Feature implementation modules must not depend on other feature implementation or entry modules directly.
- Feature implementation modules must not depend on feature data modules; feature UI code consumes domain contracts/use cases.
- Only `:app` may depend on the listed feature implementation modules for composition-root DI.
- Only `:app` may depend on listed feature data modules for composition-root DI.
- Only `:app` may depend on listed feature domain modules for composition-root DI.
- App production code may reference core data/storage and feature implementation/data/domain packages only from app-owned DI composition modules; core network wiring must be explicitly reapproved when a real data consumer exists.
- App DI may reference feature data implementations only from approved feature-data repository binding modules.
- Production Hilt modules may be declared only in app-owned DI composition modules.

## Split Decision

`routine`, `exercise`, `analysis`, and `workout` are valid feature candidates because they map to user-visible destinations or flows. The app-owned coordinator should now stay narrow: route selection, feature composition, and explicit cross-feature handoffs only. The safer path is to keep moving one cohesive destination, dialog, or flow per PR until only app-level routing and composition remain.
