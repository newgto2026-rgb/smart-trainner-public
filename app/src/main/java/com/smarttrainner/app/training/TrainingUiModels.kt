package com.smarttrainner.app.training

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId

internal data class TrainingUiState(
    val recordingPlannedExercise: PlannedExercise? = null,
    val selectedExerciseId: ExerciseId? = null,
    val recordingFlow: RecordingFlow = RecordingFlow.SINGLE,
    val skippedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
)
