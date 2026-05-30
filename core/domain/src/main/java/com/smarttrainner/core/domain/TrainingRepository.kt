package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(): Flow<List<Exercise>>
    suspend fun getExercise(id: ExerciseId): Exercise?
}

interface RoutinePlanRepository {
    fun observePlanTemplates(): Flow<List<PlanTemplate>>
    fun observeCustomRoutines(): Flow<List<PlanTemplate>>
    fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan>
}

interface RoutineProgressRepository {
    fun observeRoutineProgress(): Flow<RoutineProgress>
}

interface WorkoutLogRepository {
    fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>>
    fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>>
    suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog?
    suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit>
}

interface WeeklySummaryRepository {
    fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary>
}

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    suspend fun startDefaultSession(): Result<UserSession>
    suspend fun signOut(): Result<Unit>
}
