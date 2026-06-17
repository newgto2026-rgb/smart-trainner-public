package com.smarttrainner.core.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import kotlinx.coroutines.flow.Flow

interface WorkoutLogRepository {
    fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>>
    fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>>
    suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? = null
    suspend fun getLatestWorkoutLog(plannedExerciseId: PlannedExerciseId): WorkoutLog? = null
    suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> =
        Result.failure(UnsupportedOperationException("Workout log saving is not supported"))

    suspend fun updateWorkoutLog(id: WorkoutLogId, input: WorkoutLogInput): Result<Unit> =
        Result.failure(UnsupportedOperationException("Workout log updating is not supported"))
}
