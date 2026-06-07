package com.smarttrainner.core.model

import java.time.Instant
import java.time.LocalDate

data class CurrentRoutineCycle(
    val progress: RoutineProgress,
    val plan: CyclePlan,
    val currentDayIndex: Int,
    val currentDay: WorkoutDayPlan?,
    val currentRoutineDayInstanceId: String?,
    val currentRoutineDayDate: LocalDate?,
    val previousRoutineDayInstanceId: String?,
    val previousRoutineDayDate: LocalDate?,
    val currentCycleLogs: List<WorkoutLog>,
    val allLogs: List<WorkoutLog>,
    val currentCyclePlannedExerciseIds: Set<PlannedExerciseId>,
    val currentDayCompletedPlannedExerciseIds: Set<PlannedExerciseId>,
    val latestCompletion: CurrentRoutineCycleCompletion?
)

data class CurrentRoutineCycleCompletion(
    val day: WorkoutDayPlan,
    val cycleNumber: Int,
    val dayNumber: Int,
    val completedAt: Instant?
)
