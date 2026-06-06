package com.smarttrainner.core.model

import java.time.LocalDate

data class CycleSummary(
    val cycleStartDate: LocalDate,
    val plannedExerciseCount: Int,
    val completedExerciseCount: Int,
    val totalSets: Int,
    val totalVolumeKg: Double,
    val totalMinutes: Int,
    val streakDays: Int,
    val muscleBalance: Map<MuscleGroup, Int>,
    val insight: String
) {
    val completionRate: Int
        get() = if (plannedExerciseCount == 0) 0 else completedExerciseCount * 100 / plannedExerciseCount
}
