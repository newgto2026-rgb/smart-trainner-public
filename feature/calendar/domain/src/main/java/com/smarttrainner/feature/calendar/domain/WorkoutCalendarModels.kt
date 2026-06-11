package com.smarttrainner.feature.calendar.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class WorkoutCalendarMonth(
    val month: YearMonth,
    val summariesByDate: Map<LocalDate, WorkoutDateSummary>,
    val logsByDate: Map<LocalDate, List<WorkoutCalendarLog>>,
    val todayWorkoutCount: Int
)

data class WorkoutDateSummary(
    val date: LocalDate,
    val workoutCount: Int,
    val completedCount: Int,
    val totalSetCount: Int,
    val totalVolumeKg: Double
)

data class WorkoutCalendarLog(
    val id: WorkoutLogId,
    val exerciseId: ExerciseId,
    val exerciseName: String,
    val muscleGroup: MuscleGroup?,
    val performedAt: LocalDateTime,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val memo: String,
    val completed: Boolean,
    val volumeKg: Double,
    val setEntries: List<WorkoutSetLog> = emptyList()
)
