package com.smarttrainner.feature.workout.api

import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WorkoutLog

enum class RecordFormError {
    SELECT_EXERCISE,
    SETS,
    REPS,
    WEIGHT,
    DURATION,
    REST,
    SAVE_FAILED,
    COMPLETE_DAY_FAILED
}

data class RecordFormState(
    val setEntries: List<RecordSetFormState> = emptyList(),
    val memo: String = ""
)

data class RecordSetFormState(
    val reps: String = "",
    val weightKg: String = "",
    val durationMinutes: String = "",
    val restSeconds: String = ""
)

data class WorkoutRecordingUiState(
    val recordingPlannedExercise: PlannedExercise? = null,
    val weeklyLogs: List<WorkoutLog> = emptyList(),
    val latestWorkoutLogs: List<WorkoutLog> = emptyList(),
    val recordForm: RecordFormState = RecordFormState(),
    val formError: RecordFormError? = null,
    val recordSaved: Boolean = false
)

data class WorkoutRecordingActions(
    val onSetRepsChanged: (Int, String) -> Unit = { _, _ -> },
    val onSetWeightChanged: (Int, String) -> Unit = { _, _ -> },
    val onSetDurationChanged: (Int, String) -> Unit = { _, _ -> },
    val onSetRestChanged: (Int, String) -> Unit = { _, _ -> },
    val onAddSet: () -> Unit = {},
    val onRemoveSet: (Int) -> Unit = {},
    val onMemoChanged: (String) -> Unit = {},
    val onSaveRecord: () -> Unit = {},
    val onExerciseMethodSelected: () -> Unit = {},
    val onDismiss: () -> Unit = {}
)
