package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutDayPlan

internal fun RoutineUiState.nextPlannedExerciseAfterSaved(
    plannedExercise: PlannedExercise,
    skippedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
): PlannedExercise? = currentDayFor(plannedExercise.id)?.nextIncompleteInSameDay(
    currentId = plannedExercise.id,
    completedIds = completedPlannedExerciseIds +
        this.skippedPlannedExerciseIds +
        skippedPlannedExerciseIds +
        plannedExercise.id
)

internal fun RoutineUiState.nextPlannedExerciseAfterSkipped(
    plannedExercise: PlannedExercise,
    skippedPlannedExerciseIds: Set<PlannedExerciseId>
): PlannedExercise? = currentDayFor(plannedExercise.id)?.nextIncompleteInSameDay(
    currentId = plannedExercise.id,
    completedIds = completedPlannedExerciseIds + this.skippedPlannedExerciseIds + skippedPlannedExerciseIds
)

internal fun RoutineUiState.recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise? =
    if (customRoutineBuilder.visible) {
        null
    } else {
        nextRoutineDayUi?.previewExercises?.firstOrNull { planned ->
            planned.exercise.id == exerciseId &&
                planned.id !in completedPlannedExerciseIds &&
                planned.id !in skippedPlannedExerciseIds
        }
    }

internal fun RoutineUiState.isPlanExerciseCompleted(
    dayIndex: Int,
    plannedExercise: PlannedExercise
): Boolean = plannedExercise.id in completedPlannedExerciseIds || isRoutineDayCompleted(dayIndex)

internal fun RoutineUiState.isPlanExerciseSkipped(
    dayIndex: Int,
    plannedExercise: PlannedExercise,
    hasRecordedLog: Boolean
): Boolean =
    (
        plannedExercise.id in skippedPlannedExerciseIds ||
            (isRoutineDayCompleted(dayIndex) && !hasRecordedLog)
        ) &&
        plannedExercise.id !in completedPlannedExerciseIds

internal fun RoutineUiState.withSessionProgress(
    recordedPlannedExerciseIds: Set<PlannedExerciseId>,
    skippedPlannedExerciseIds: Set<PlannedExerciseId>
): RoutineUiState {
    if (recordedPlannedExerciseIds.isEmpty() && skippedPlannedExerciseIds.isEmpty()) return this
    val effectiveCompletedIds = completedPlannedExerciseIds + recordedPlannedExerciseIds
    val effectiveSkippedIds = this.skippedPlannedExerciseIds + skippedPlannedExerciseIds
    val currentNextRoutineDayUi = nextRoutineDayUi
    val updatedNextRoutineDay = currentNextRoutineDayUi?.let { nextDayUi ->
        val previewExercises = nextDayUi.previewExercises
        nextDayUi.copy(
            completedExerciseCount = previewExercises.count { it.id in effectiveCompletedIds },
            startExercise = previewExercises
                .firstOrNull { it.id !in effectiveCompletedIds && it.id !in effectiveSkippedIds }
                ?: previewExercises.firstOrNull()
        )
    }
    return copy(
        nextRoutineDayUi = updatedNextRoutineDay,
        nextRoutineDay = updatedNextRoutineDay?.day ?: nextRoutineDay,
        completedPlannedExerciseIds = effectiveCompletedIds,
        skippedPlannedExerciseIds = effectiveSkippedIds
    )
}

private fun RoutineUiState.currentDayFor(currentId: PlannedExerciseId): WorkoutDayPlan? =
    nextRoutineDayUi?.day?.takeIf { day -> day.exercises.any { it.id == currentId } }

private fun RoutineUiState.isRoutineDayCompleted(dayIndex: Int): Boolean {
    val progress = activeRoutineProgress ?: return false
    val lastCompletedDayIndex = progress.lastCompletedDayIndex ?: return false
    val lastCompletedCycleNumber = progress.lastCompletedCycleNumber ?: progress.cycleNumber
    return lastCompletedCycleNumber == progress.cycleNumber && dayIndex <= lastCompletedDayIndex
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
