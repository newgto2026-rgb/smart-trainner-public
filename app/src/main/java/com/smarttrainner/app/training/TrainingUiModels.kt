package com.smarttrainner.app.training

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise

data class TrainingUiState(
    val recordingPlannedExercise: PlannedExercise? = null,
    val selectedExerciseId: ExerciseId? = null
)
