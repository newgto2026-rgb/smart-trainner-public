package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun observeExercises(): Flow<List<Exercise>>
    fun observePlanTemplates(): Flow<List<PlanTemplate>>
    fun observeCustomRoutines(): Flow<List<PlanTemplate>>
    fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan>
    fun observeRoutineProgress(): Flow<RoutineProgress>
    fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>>
    fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>>
    fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary>
    suspend fun getExercise(id: ExerciseId): Exercise?
    suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog?
    suspend fun selectPlanTemplate(templateId: String): Result<Unit>
    suspend fun startRoutine(templateId: String): Result<Unit>
    suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate>
    suspend fun deleteCustomRoutine(templateId: String): Result<Unit>
    suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit>
    suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit>
}

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    suspend fun startDefaultSession(): Result<UserSession>
    suspend fun signOut(): Result<Unit>
}
