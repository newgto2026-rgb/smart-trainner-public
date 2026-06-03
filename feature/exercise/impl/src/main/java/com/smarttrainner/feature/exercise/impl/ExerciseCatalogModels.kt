package com.smarttrainner.feature.exercise.impl

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.WorkoutLog

internal data class ExerciseCatalogUiState(
    val exercises: List<Exercise> = emptyList(),
    val latestWorkoutLogs: List<WorkoutLog> = emptyList(),
    val searchQuery: String = "",
    val selectedExerciseId: ExerciseId? = null
)
