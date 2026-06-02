package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WeeklyPlan

internal fun RoutineUiState.nextPlannedExerciseAfterSaved(
    plannedExercise: PlannedExercise,
    skippedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
): PlannedExercise? = plan?.nextIncompleteInSameDay(
    currentId = plannedExercise.id,
    completedIds = completedPlannedExerciseIds + skippedPlannedExerciseIds + plannedExercise.id
)

internal fun RoutineUiState.nextPlannedExerciseAfterSkipped(
    plannedExercise: PlannedExercise,
    skippedPlannedExerciseIds: Set<PlannedExerciseId>
): PlannedExercise? = plan?.nextIncompleteInSameDay(
    currentId = plannedExercise.id,
    completedIds = completedPlannedExerciseIds + skippedPlannedExerciseIds
)

internal fun RoutineUiState.recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise? =
    if (customRoutineBuilder.visible) {
        null
    } else {
        plan?.plannedExerciseFor(exerciseId)
    }

private fun WeeklyPlan.plannedExerciseFor(exerciseId: ExerciseId): PlannedExercise? =
    days.firstNotNullOfOrNull { day ->
        day.exercises.firstOrNull { it.exercise.id == exerciseId }
    }
