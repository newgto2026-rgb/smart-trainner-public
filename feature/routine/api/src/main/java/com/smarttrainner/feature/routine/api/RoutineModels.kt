package com.smarttrainner.feature.routine.api

import com.smarttrainner.core.domain.RoutineSessionCoordinator
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId

data class RoutineFeatureCallbacks(
    val onWorkoutStarted: (PlannedExercise) -> Unit = {},
    val onRoutineDayCompleted: () -> Unit = {},
    val onExerciseMethodSelected: (ExerciseId) -> Unit = {},
    val onRecordSelected: (PlannedExercise) -> Unit = {},
    val onSubstituteExerciseSelected: (PlannedExercise) -> Unit = {},
    val onAdditionalExerciseSelected: (PlannedExercise) -> Unit = {},
    val routineLibraryOpenRequest: Int = 0,
    val onRoutineLibraryOpenRequestConsumed: (Int) -> Unit = {},
    val sessionRecordedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet(),
    val sessionSkippedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
)

interface RoutineRouteState {
    val currentRoutineName: String
    val sessionCoordinator: RoutineSessionCoordinator
}
