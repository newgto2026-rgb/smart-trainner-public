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

## Split Decision

`routine`, `exercise`, `analysis`, and `workout` are valid feature candidates because they map to user-visible destinations or flows. They should not be split all at once while a single `TrainingViewModel` still coordinates routine progress, exercise selection, and recording forms. The safer path is to land the app shell first, then move one cohesive destination or flow per PR.
