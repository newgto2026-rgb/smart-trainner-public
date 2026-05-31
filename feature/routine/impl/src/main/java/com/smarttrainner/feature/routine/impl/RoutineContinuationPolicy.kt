package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WeeklyPlan

internal fun RoutineUiState.nextPlannedExerciseAfterSaved(
    plannedExercise: PlannedExercise
): PlannedExercise? = plan?.nextIncompleteInSameDay(
    currentId = plannedExercise.id,
    completedIds = completedPlannedExerciseIds + plannedExercise.id
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
