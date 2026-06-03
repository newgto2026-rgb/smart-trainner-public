package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExerciseId
import java.time.Instant

data class RoutineCompletionSnapshot(
    val dayIndex: Int,
    val completedAt: Instant?,
    val cycleNumber: Int,
    val previousCycleStartedAt: Instant?,
    val cycleDurationDays: Int? = null
)

interface RoutinePlanCommandRepository {
    suspend fun selectPlanTemplate(templateId: String): Result<Unit>
    suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate>
    suspend fun deleteCustomRoutine(templateId: String): Result<Unit>
}

interface RoutineProgressCommandRepository {
    suspend fun startRoutine(templateId: String): Result<Unit>
    suspend fun switchRoutineTemplate(templateId: String): Result<Unit>
    suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit>

    suspend fun cancelLatestRoutineDayCompletion(
        restoredDayIndex: Int,
        restoredCycleNumber: Int,
        restoredCycleStartedAt: Instant?,
        remainingLatestCompletion: RoutineCompletionSnapshot?,
        plannedExerciseIds: Set<PlannedExerciseId>,
        additionalExerciseIdPrefix: String
    ): Result<Unit>
}
