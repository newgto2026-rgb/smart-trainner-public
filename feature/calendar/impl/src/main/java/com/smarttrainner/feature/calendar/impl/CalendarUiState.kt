package com.smarttrainner.feature.calendar.impl

import androidx.compose.runtime.Immutable
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseLoadType
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Immutable
internal data class CalendarUiState(
    val currentMonth: YearMonth,
    val selectedDate: LocalDate,
    val isMonthExpanded: Boolean = true,
    val days: List<CalendarDayUiModel>,
    val selectedWeekDays: List<CalendarDayUiModel> = emptyList(),
    val todayWorkoutCount: Int,
    val selectedDateWorkouts: List<CalendarSelectedWorkoutUiModel>,
    val editor: CalendarWorkoutEditorUiState? = null
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
    val exerciseId: ExerciseId,
    val plannedExerciseId: PlannedExerciseId,
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
    val loadType: ExerciseLoadType,
    val effectiveVolumeKg: Double?,
    val effectiveSetLoadsKg: List<Double> = emptyList(),
    val setEntries: List<WorkoutSetLog> = emptyList(),
    val routineDayInstanceId: String? = null
)

internal enum class CalendarWorkoutEditorMode {
    ADD,
    EDIT
}

@Immutable
internal data class CalendarWorkoutEditorUiState(
    val mode: CalendarWorkoutEditorMode,
    val selectedDate: LocalDate,
    val exerciseOptions: List<CalendarExerciseOptionUiModel>,
    val selectedExerciseId: ExerciseId?,
    val selectedExerciseName: String,
    val selectedExerciseLoadType: ExerciseLoadType,
    val showReps: Boolean,
    val showWeight: Boolean,
    val showDuration: Boolean,
    val setEntries: List<CalendarWorkoutSetFormUiState>,
    val memo: String,
    val error: CalendarWorkoutEditorError? = null,
    val isSaving: Boolean = false
)

@Immutable
internal data class CalendarExerciseOptionUiModel(
    val id: ExerciseId,
    val name: String
)

@Immutable
internal data class CalendarWorkoutSetFormUiState(
    val reps: String = "",
    val weightKg: String = "",
    val durationMinutes: String = "",
    val restSeconds: String = ""
)

internal enum class CalendarWorkoutEditorError {
    EXERCISE,
    SETS,
    REPS,
    WEIGHT,
    DURATION,
    REST,
    SAVE_FAILED
}
