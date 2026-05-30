package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.PlanTemplate
import java.time.Instant

interface RoutinePlanCommandRepository {
    suspend fun selectPlanTemplate(templateId: String): Result<Unit>
    suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate>
    suspend fun deleteCustomRoutine(templateId: String): Result<Unit>
}

interface RoutineProgressCommandRepository {
    suspend fun startRoutine(templateId: String): Result<Unit>
    suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit>
}
