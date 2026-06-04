package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.estimatedSessionMinutes
import com.smarttrainner.feature.routine.domain.routineDayInstanceId

internal fun com.smarttrainner.core.model.WorkoutDayPlan.toNextRoutineDayUiModel(
    template: PlanTemplate?,
    dayIndex: Int,
    cycleNumber: Int,
    completedIds: Set<PlannedExerciseId>
): NextRoutineDayUiModel {
    val nextDay = template?.days?.takeIf { it.isNotEmpty() }?.let { days ->
        days.getOrNull((dayIndex + 1) % days.size)
    }
    val dayInstanceId = template?.let {
        routineDayInstanceId(
            templateId = it.id,
            cycleNumber = cycleNumber,
            dayNumber = dayNumber
        )
    }
    val instanceExercises = exercises.map { exercise ->
        exercise.copy(routineDayInstanceId = dayInstanceId)
    }
    return NextRoutineDayUiModel(
        day = copy(exercises = instanceExercises),
        routineTemplate = template,
        primaryFocus = primaryFocus,
        secondaryFocuses = secondaryFocuses,
        cycleNumber = cycleNumber,
        dayNumber = dayNumber,
        focus = focus,
        sessionMinutes = estimatedSessionMinutes.takeIf { it > 0 } ?: template?.sessionMinutes ?: 45,
        previewExercises = instanceExercises,
        startExercise = instanceExercises.firstOrNull { it.id !in completedIds } ?: instanceExercises.firstOrNull(),
        nextPrimaryFocus = nextDay?.primaryFocus,
        completedExerciseCount = instanceExercises.count { it.id in completedIds },
        totalExerciseCount = instanceExercises.size,
        minRecoveryHours = minRecoveryHours
    )
}

fun WeeklyPlan.findPlannedExercise(id: PlannedExerciseId?): PlannedExercise? {
    if (id == null) return null
    return days.asSequence()
        .flatMap { it.exercises.asSequence() }
        .firstOrNull { it.id == id }
}

fun WeeklyPlan.firstIncomplete(completedIds: Set<PlannedExerciseId>): PlannedExercise? =
    days.asSequence()
        .flatMap { it.exercises.asSequence() }
        .firstOrNull { it.id !in completedIds }
        ?: days.firstOrNull()?.exercises?.firstOrNull()

fun WeeklyPlan.nextIncompleteInSameDay(
    currentId: PlannedExerciseId,
    completedIds: Set<PlannedExerciseId>
): PlannedExercise? {
    val day = days.firstOrNull { workoutDay -> workoutDay.exercises.any { it.id == currentId } } ?: return null
    val currentIndex = day.exercises.indexOfFirst { it.id == currentId }
    return day.exercises
        .drop(currentIndex + 1)
        .firstOrNull { it.id !in completedIds }
}
