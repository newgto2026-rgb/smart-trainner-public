package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutDayPlan

internal fun RoutineUiState.nextPlannedExerciseAfterSaved(
    plannedExercise: PlannedExercise,
    skippedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
): PlannedExercise? = currentDayFor(plannedExercise.id)?.nextIncompleteInSameDay(
    currentId = plannedExercise.id,
    completedIds = completedPlannedExerciseIds + skippedPlannedExerciseIds + plannedExercise.id
)

internal fun RoutineUiState.nextPlannedExerciseAfterSkipped(
    plannedExercise: PlannedExercise,
    skippedPlannedExerciseIds: Set<PlannedExerciseId>
): PlannedExercise? = currentDayFor(plannedExercise.id)?.nextIncompleteInSameDay(
    currentId = plannedExercise.id,
    completedIds = completedPlannedExerciseIds + skippedPlannedExerciseIds
)

internal fun RoutineUiState.recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise? =
    if (customRoutineBuilder.visible) {
        null
    } else {
        nextRoutineDayUi?.previewExercises?.firstOrNull { it.exercise.id == exerciseId }
            ?: plan?.plannedExerciseFor(exerciseId)
    }

private fun RoutineUiState.currentDayFor(currentId: PlannedExerciseId): WorkoutDayPlan? =
    nextRoutineDayUi?.day?.takeIf { day -> day.exercises.any { it.id == currentId } }
        ?: plan?.days?.firstOrNull { day -> day.exercises.any { it.id == currentId } }

private fun WeeklyPlan.plannedExerciseFor(exerciseId: ExerciseId): PlannedExercise? =
    days.firstNotNullOfOrNull { day ->
        day.exercises.firstOrNull { it.exercise.id == exerciseId }
    }

private fun WorkoutDayPlan.nextIncompleteInSameDay(
    currentId: PlannedExerciseId,
    completedIds: Set<PlannedExerciseId>
): PlannedExercise? {
    val currentIndex = exercises.indexOfFirst { it.id == currentId }
    return exercises
        .drop(currentIndex + 1)
        .firstOrNull { it.id !in completedIds }
}
