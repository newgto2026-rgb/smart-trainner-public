package com.smarttrainner.feature.workout.impl

import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WorkoutLog

internal enum class RecordFormError {
    SELECT_EXERCISE,
    SETS,
    REPS,
    WEIGHT,
    DURATION,
    REST,
    SAVE_FAILED,
    COMPLETE_DAY_FAILED
}

internal data class RecordFormState(
    val setEntries: List<RecordSetFormState> = emptyList(),
    val memo: String = ""
)

internal data class RecordSetFormState(
    val reps: String = "",
    val weightKg: String = "",
    val durationMinutes: String = "",
    val restSeconds: String = ""
)

internal data class WorkoutRecordingUiState(
    val recordingPlannedExercise: PlannedExercise? = null,
    val showRoutineSessionActions: Boolean = false,
    val hasNextPlannedExercise: Boolean = false,
    val cycleLogs: List<WorkoutLog> = emptyList(),
    val latestWorkoutLogs: List<WorkoutLog> = emptyList(),
    val recordForm: RecordFormState = RecordFormState(),
    val formError: RecordFormError? = null,
    val recordSaved: Boolean = false
)

internal data class WorkoutRecordingActions(
    val onSetRepsChanged: (Int, String) -> Unit = { _, _ -> },
    val onSetWeightChanged: (Int, String) -> Unit = { _, _ -> },
    val onSetDurationChanged: (Int, String) -> Unit = { _, _ -> },
    val onSetRestChanged: (Int, String) -> Unit = { _, _ -> },
    val onAddSet: () -> Unit = {},
    val onRemoveSet: (Int) -> Unit = {},
    val onMemoChanged: (String) -> Unit = {},
    val onSaveRecord: () -> Unit = {},
    val onSkipExercise: () -> Unit = {},
    val onSubstituteExerciseRequested: () -> Unit = {},
    val onAddExerciseRequested: () -> Unit = {},
    val onExerciseMethodSelected: () -> Unit = {},
    val onDismiss: () -> Unit = {}
)
