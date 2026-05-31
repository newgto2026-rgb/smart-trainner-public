package com.smarttrainner.feature.routine.api

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise

data class RoutineFeatureCallbacks(
    val onWorkoutStarted: (PlannedExercise) -> Unit = {},
    val onRoutineDayCompleted: () -> Unit = {},
    val onExerciseMethodSelected: (ExerciseId) -> Unit = {},
    val onRecordSelected: (PlannedExercise) -> Unit = {}
)

interface RoutineRouteState {
    val currentRoutineName: String

    fun nextPlannedExerciseAfterSaved(plannedExercise: PlannedExercise): PlannedExercise?

    fun recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise?
}
