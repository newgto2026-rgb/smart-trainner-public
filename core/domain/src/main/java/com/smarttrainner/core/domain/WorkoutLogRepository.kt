package com.smarttrainner.core.domain

import com.smarttrainner.core.model.WorkoutLog
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface WorkoutLogRepository {
    fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>>
    fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>>
    fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>>
}
