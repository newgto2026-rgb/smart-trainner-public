package com.smarttrainner.feature.routine.api

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId

data class RoutineFeatureCallbacks(
    val onWorkoutStarted: (PlannedExercise) -> Unit = {},
    val onRoutineDayCompleted: () -> Unit = {},
    val onExerciseMethodSelected: (ExerciseId) -> Unit = {},
    val onRecordSelected: (PlannedExercise) -> Unit = {},
    val onSubstituteExerciseSelected: (PlannedExercise) -> Unit = {},
    val onAdditionalExerciseSelected: (PlannedExercise) -> Unit = {}
)

interface RoutineRouteState {
    val currentRoutineName: String

    fun nextPlannedExerciseAfterSaved(
        plannedExercise: PlannedExercise,
        skippedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
    ): PlannedExercise?

    fun nextPlannedExerciseAfterSkipped(
        plannedExercise: PlannedExercise,
        skippedPlannedExerciseIds: Set<PlannedExerciseId>
    ): PlannedExercise?

    fun requestCompleteRoutineDay(
        skippedPlannedExerciseIds: Set<PlannedExerciseId>,
        justRecordedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
    )

    fun requestSubstituteExercise(plannedExercise: PlannedExercise)

    fun requestAdditionalExercise(anchorExercise: PlannedExercise?)

    fun recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise?
}
