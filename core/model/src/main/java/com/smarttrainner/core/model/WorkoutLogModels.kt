package com.smarttrainner.core.model

import java.time.LocalDateTime

data class WorkoutLog(
    val id: WorkoutLogId,
    val sessionId: UserSessionId,
    val plannedExerciseId: PlannedExerciseId,
    val exerciseId: ExerciseId,
    val performedAt: LocalDateTime,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val memo: String,
    val completed: Boolean,
    val setEntries: List<WorkoutSetLog> = emptyList(),
    val routineDayInstanceId: String? = null
) {
    val volumeKg: Double
        get() = if (setEntries.isNotEmpty()) {
            setEntries.sumOf { it.volumeKg }
        } else if (reps != null && weightKg != null) {
            sets * reps * weightKg
        } else {
            0.0
        }
}

data class WorkoutLogInput(
    val plannedExerciseId: PlannedExerciseId,
    val exerciseId: ExerciseId,
    val performedAt: LocalDateTime,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val memo: String,
    val completed: Boolean,
    val setEntries: List<WorkoutSetLog> = emptyList(),
    val routineDayInstanceId: String? = null
)

data class WorkoutSetLog(
    val order: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val restSeconds: Int? = null
) {
    val volumeKg: Double
        get() = if (reps != null && weightKg != null) reps * weightKg else 0.0
}
