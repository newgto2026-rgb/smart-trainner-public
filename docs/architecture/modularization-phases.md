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

## Strict Feature Isolation Audit

Current state is strict at the feature-module dependency level. State ownership is now feature-owned for the major destination and dialog surfaces, with app keeping only cross-feature coordination:

- `:feature:analysis`, `:feature:exercise`, `:feature:routine`, and `:feature:workout` no longer depend on another feature's API, implementation, or entry module.
- `:app` knows feature APIs for routing and listed feature implementations only for app-owned DI composition.
- App `TrainingViewModel` now coordinates selected exercise id and a generic single/continuous recording flow; workout recording, exercise detail, exercise catalog, analysis, routine, custom routine builder state, and routine continuation policy are feature-owned.
- App no longer imports routine UI/action/form models or raw routine coordinator state; it consumes only the routine feature entry and opaque route API.
- App no longer owns the routine/exercise `LazyListScope` screen assembly; shared screen chrome lives in `:core:ui`, and feature APIs expose composable route surfaces.
- Core domain persistence contracts are no longer one broad `TrainingRepository`; use cases depend on concern-specific shared contracts.
- Core data repository implementations now mirror those concern-specific contracts instead of one catch-all implementation.
- App-owned DI composition now binds shared core-domain repository contracts to their core-data implementations.
- App-owned DI composition now owns production platform providers for Room, Retrofit, and app-wide time.
- Analysis API now exposes only a route entry; its content-rendering surface and UI state models are implementation details.
- Workout recording API now exposes only its dialog route entry; its form state, validation errors, and rendering actions are implementation details.
- Exercise detail API now exposes only its dialog route entry; its detail UI state and rendering actions are implementation details.
- `:feature:*:entry` modules have been removed; Hilt feature-entry bindings now live in the app composition root.

Current guardrails still enforce the important lower-level boundary:

- `core:*` must not depend on `feature:*`.
- Feature modules must not depend on data, storage, or network implementation modules.
- New feature-local domain/data/network modules require an explicit ownership decision before being introduced.
- Feature API modules must not depend on feature implementation or entry modules.
- Feature implementation modules must not depend on other feature implementation or entry modules directly.
- Only `:app` may depend on the listed feature implementation modules for composition-root DI.
- App production code may reference core data/storage/network implementation packages only from app-owned DI composition modules.
- Production Hilt modules may be declared only in app-owned DI composition modules.

## Split Decision

`routine`, `exercise`, `analysis`, and `workout` are valid feature candidates because they map to user-visible destinations or flows. The app-owned coordinator should now stay narrow: route selection, feature composition, and explicit cross-feature handoffs only. The safer path is to keep moving one cohesive destination, dialog, or flow per PR until only app-level routing and composition remain.
