# Custom Exercise Decision Rounds

## Round 1: Product Scope
Decision: Implement private custom exercise create, read, update, delete, catalog exposure, routine/workout integration, and metadata sync. Friend sharing UI remains out of scope.

Rationale: Local-only custom exercises would break cross-device and server-side routine/log interpretation.

## Round 2: Ownership
Decision: Use `ownerSessionId` as the access boundary. Seed exercises have no owner. Future sharing must reference `(ownerSessionId, exerciseId)`.

Rationale: Encoding owner into IDs would make future friend sharing and copying brittle.

## Round 3: Exercise ID
Decision: Use client-generated IDs with `custom_exercise_` prefix. Reject collisions with seed and owner custom IDs.

Rationale: Stable IDs preserve routine and workout references across edits.

## Round 4: Image Input
Decision: Support image URL input and local image picking on Android, but do not upload image binaries to the server in this PR.

Rationale: Product expects image input, but server upload and image storage need a separate contract. Local images remain device-local; remote URLs can sync.

## Round 5: Default Image
Decision: Replace the inappropriate "image QA pending" copy for custom exercises with a default image placeholder state.

Rationale: User-created exercises are not awaiting official QA; they simply may not have an image.

## Round 6: Storage
Decision: Add a dedicated `custom_exercises` Room table with sync state and `archivedAt`.

Rationale: Custom exercises need session isolation and historical lookup independent of seed catalog changes.

## Round 7: Catalog Merge
Decision: `observeExercises()` returns seeded exercises plus active current-owner custom exercises. Confirmed deletion prunes dependent local references instead of keeping archived custom exercises in user-visible lookup paths.

Rationale: Catalogs should stay clean and routine/log rendering should not depend on hidden custom exercise rows after a destructive user-confirmed delete.

## Round 8: Routine And Workout
Decision: Custom exercises are selectable in custom routines and workout recording. Cycle plan generation must use the merged catalog.

Rationale: A custom exercise is useful only if it behaves like a first-class exercise after creation.

## Round 9: Sync Order
Decision: Custom exercise metadata sync runs before custom routine and workout log sync.

Rationale: Dependent routine/log payloads should not reach the server before their custom exercise definitions.

## Round 10: Form Design
Decision: Use a full-height dialog/editor from the Exercise tab. Dropdowns are used for muscle group/category, equipment, and difficulty. Dynamic lists are used for method steps and safety points.

Rationale: The form is too dense for a compact dialog, but does not need a new top-level navigation route.

## Round 11: Testing
Decision: Require unit, DAO, repository/sync, server, and connected UI coverage. UI tests cover create/validation, routine exposure, image fallback, and session isolation.

Rationale: This feature crosses catalog, storage, server, routine, and UI boundaries.

## Round 12: Deletion Semantics
Decision: Expose deletion only from the custom exercise detail dialog. The confirmation copy must warn that related custom routines and workout records can be changed. Confirmed deletion hides the custom exercise and removes same-owner routine entries and workout logs.

Rationale: Keeping dangling routine/log references would make cycle plan rendering and later sync brittle. The detail dialog is the safest place to show a destructive owner-only action.

## Rejected Options
- Local-only custom exercise storage: rejected because server routines/logs would not understand custom exercise IDs.
- URL-only image input: rejected because it weakly satisfies the image input requirement.
- Server binary image upload in this PR: rejected because storage, size limits, and moderation need a separate contract.
- Silent hard delete: rejected because routines and workout logs would change without informed consent.
- Owner-encoded exercise IDs: rejected because future sharing needs identity and authorization separated.
