package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
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

interface WeeklyPlanRepository {
    fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan>
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
