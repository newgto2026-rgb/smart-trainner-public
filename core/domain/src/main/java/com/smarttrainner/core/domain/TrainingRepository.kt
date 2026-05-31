package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutLog
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
}

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    suspend fun startDefaultSession(): Result<UserSession>
}
