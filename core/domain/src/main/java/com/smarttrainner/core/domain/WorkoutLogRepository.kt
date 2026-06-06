package com.smarttrainner.core.domain

import com.smarttrainner.core.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

interface WorkoutLogRepository {
    fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>>
    fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>>
}
