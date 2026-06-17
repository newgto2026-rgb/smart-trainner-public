# Custom Exercise PRD/TRD

## PRD

### Problem
Users can only choose from the seeded exercise catalog. When they use a personal machine, rehab drill, variation, or gym-specific movement, they cannot save it as a first-class exercise for catalog search, routine building, workout recording, and later analysis.

### Goal
Add private custom exercises owned by the active logged-in user. A custom exercise should behave like a seeded exercise inside the app, while remaining invisible to other users unless a future explicit sharing feature grants access.

### User Flow
1. User opens the Exercise tab.
2. User taps "Add custom exercise".
3. User enters name, category, equipment, difficulty, summary, default target, method steps, optional safety points, and an optional image.
4. Category, equipment, and difficulty are selected from dropdowns.
5. If an image URL or picked image is present, the image preview uses the current exercise image frame. If not, a default image placeholder is shown.
6. User saves the exercise.
7. The custom exercise appears in Exercise tab search, detail, workout recording, and custom routine exercise pickers for that user only.
8. User can delete their own custom exercise later after confirming the routine/workout-record impact.

### Acceptance Criteria
- A custom exercise is scoped to the active `ownerSessionId`.
- Another session on the same device does not see the custom exercise.
- Custom exercise IDs remain stable when name or content changes.
- Deleted custom exercises disappear from catalog/pickers, are removed from same-owner custom routine entries, and remove workout records saved with that exercise.
- Deleting a custom exercise requires a warning dialog that names the related routine and workout-record impact before the destructive action runs.
- Category, equipment, and difficulty are dropdown selections.
- A custom exercise can be saved without an image and renders a default image placeholder.
- A custom exercise can render an image URL or locally picked image in the same 0.9 image frame used by seeded exercises.
- Custom exercises can be selected in custom routines and recorded in workout flows.
- Server metadata sync stores user-owned custom exercise definitions without exposing them to other users.
- Future friend sharing can reference `(ownerSessionId, exerciseId)` without copying or colliding with local owner-created exercises.

### Non-Goals
- Public exercise marketplace.
- Friend sharing UI.
- AI-generated exercise content.
- Server-side binary image upload.
- Full image editing/cropping UI.
- Medical safety review of user-entered content.
- Multi-image step gallery upload.

## TRD

### Ownership Model
- Seeded exercises use `ExerciseSource.SYSTEM` and no owner.
- User-created exercises use `ExerciseSource.USER_CREATED` and `ownerSessionId`.
- Future shared exercises may use `ExerciseSource.SHARED` or a separate share permission table, but this PR only exposes current owner exercises.
- Resource identity for future sharing is `(ownerSessionId, exerciseId)`.

### ID Model
- Custom exercise IDs are client-generated stable IDs using the reserved prefix `custom_exercise_`.
- Owner identity is stored as data, not encoded into the ID.
- ID collision with seed exercises or existing owner custom exercises is rejected.

### Android Data Model
- Extend `Exercise` with source, owner, origin, created/updated/archive metadata, and optional image URI.
- Add `CustomExerciseInput` and validation errors in `core:model`/`core:domain`.
- Add `custom_exercises` Room table and DAO.
- `ExerciseRepository.observeExercises()` returns seeded exercises plus active, non-archived custom exercises for the current session.
- `ExerciseRepository.getExercise()` resolves active custom exercises only; confirmed deletion removes dependent local references so historical lookups do not point at missing custom exercise rows.

### Server Contract
- Keep `GET /api/exercises` as the public seeded catalog.
- Add session-scoped custom exercise endpoints:
  - `GET /api/custom-exercises`
  - `POST /api/custom-exercises`
  - `PUT /api/custom-exercises/{id}`
- `DELETE /api/custom-exercises/{id}` archives the resource and removes same-session custom routine references and workout logs for that exercise.
- Responses stay wrapped in `{ data: ... }`; list responses include `count`.
- Server validates ownership by request session and never returns another user's private custom exercises.

### Image Policy
- Android supports an optional image URL text field and a local image picker.
- Local picked images are rendered from the stored URI on the current device.
- Server metadata sync only syncs remote image URLs, not local content URIs.
- If image loading fails or no image is provided, the renderer shows a custom exercise default image placeholder.
- All images use the existing exercise media frame and `ContentScale.Fit`.

### Routine And Workout Integration
- Custom exercises flow through the same `ObserveExercisesUseCase`.
- Routine builders and exercise pickers consume the merged exercise catalog.
- Cycle plan building must use the merged catalog, not the seed-only map.
- Sync order must run custom exercise sync before custom routine and workout log sync.
- Confirmed custom exercise deletion prunes local routine references and workout logs immediately, then the custom exercise delete sync performs the same server cleanup before routine/log sync fetches remote state.

### Validation
- Required: name, muscle group/category, equipment, difficulty, at least one method step, sets, rest, and either reps or duration. Safety points are optional.
- Summary is allowed to be blank; the UI stores a default summary based on the name when blank.
- Rep range must be positive and ordered.
- Duration must be positive if used.
- Sets and rest must remain within app bounds.

### Test Matrix
- Domain validation tests.
- Room DAO session isolation, delete hiding, reference pruning, and pending sync tests.
- Repository merge and `getExercise()` tests.
- Cycle plan/custom routine tests with custom exercise IDs.
- Android UI tests for create/validation, routine picker exposure, and session isolation.
- Server tests for create/list/update/delete/session isolation/id conflict and reference pruning.
