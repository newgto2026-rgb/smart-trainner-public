package com.smarttrainner.core.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId

interface RoutineSessionCoordinator {
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

    fun requestRecordSelected(plannedExercise: PlannedExercise)

    fun recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise?
}
