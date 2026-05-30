package com.smarttrainner.feature.exercise.impl

import androidx.compose.runtime.Immutable
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.WorkoutLog

@Immutable
internal data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val latestWorkoutLog: WorkoutLog? = null,
    val showRecordAction: Boolean = false
)

@Immutable
internal data class ExerciseDetailActions(
    val onDismiss: () -> Unit = {},
    val onRecordRequested: (ExerciseId) -> Unit = {}
)
