package com.smarttrainner.feature.exercise.api

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.WorkoutLog

data class ExerciseCatalogUiState(
    val exercises: List<Exercise> = emptyList(),
    val latestWorkoutLogs: List<WorkoutLog> = emptyList(),
    val selectedExerciseId: ExerciseId? = null
)

data class ExerciseCatalogActions(
    val onExerciseSelected: (ExerciseId) -> Unit = {}
)
