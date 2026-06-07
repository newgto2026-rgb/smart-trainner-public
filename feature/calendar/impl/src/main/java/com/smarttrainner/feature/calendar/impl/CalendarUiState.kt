package com.smarttrainner.feature.calendar.impl

import androidx.compose.runtime.Immutable
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WorkoutLogId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Immutable
internal data class CalendarUiState(
    val currentMonth: YearMonth,
    val selectedDate: LocalDate,
    val days: List<CalendarDayUiModel>,
    val todayWorkoutCount: Int,
    val selectedDateWorkouts: List<CalendarSelectedWorkoutUiModel>
)

@Immutable
internal data class CalendarDayUiModel(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val workoutCount: Int,
    val completedCount: Int
)

@Immutable
internal data class CalendarSelectedWorkoutUiModel(
    val id: WorkoutLogId,
    val exerciseName: String,
    val muscleGroup: MuscleGroup?,
    val performedAt: LocalDateTime,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val memo: String,
    val completed: Boolean,
    val volumeKg: Double
)
