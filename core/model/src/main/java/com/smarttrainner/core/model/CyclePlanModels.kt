package com.smarttrainner.core.model

import java.time.LocalDate

data class CyclePlan(
    val id: PlanId,
    val templateId: String,
    val name: String,
    val cycleStartDate: LocalDate,
    val days: List<WorkoutDayPlan>
)

data class WorkoutDayPlan(
    val date: LocalDate,
    val title: String,
    val focus: String,
    val exercises: List<PlannedExercise>,
    val dayNumber: Int = 1,
    val primaryFocus: RoutineFocus? = RoutineFocus.FULL_BODY,
    val secondaryFocuses: List<RoutineFocus> = emptyList(),
    val minRecoveryHours: Int = 24
)

data class PlannedExercise(
    val id: PlannedExerciseId,
    val exercise: Exercise,
    val sets: Int,
    val repRange: IntRange?,
    val durationMinutes: Int?,
    val restSeconds: Int,
    val note: String,
    val routineDayInstanceId: String? = null,
    val routineDayDate: LocalDate? = null
) {
    val targetText: String
        get() = if (repRange != null) {
            val displayRepRange = repRange.toRecommendedDisplayRepRange()
            "${sets}세트 x ${displayRepRange.first}-${displayRepRange.last}회"
        } else {
            "${sets}세트 x ${durationMinutes ?: 10}분"
        }

    val estimatedActiveSecondsPerSet: Int
        get() = durationMinutes?.let { it * SECONDS_PER_MINUTE }
            ?: ((repRange?.last ?: 0) * exercise.defaultRepDurationSeconds)

    val estimatedSecondsPerSet: Int
        get() = estimatedActiveSecondsPerSet + restSeconds

    val estimatedTotalSeconds: Int
        get() = estimateExerciseSeconds(
            sets = sets,
            repRange = repRange,
            durationMinutes = durationMinutes,
            restSeconds = restSeconds,
            repDurationSeconds = exercise.defaultRepDurationSeconds
        )

    val estimatedMinutes: Int
        get() = estimatedTotalSeconds.roundUpToMinutes()
}

val WorkoutDayPlan.estimatedSessionMinutes: Int
    get() = exercises.sumOf { it.estimatedTotalSeconds }.roundUpToMinutes()
