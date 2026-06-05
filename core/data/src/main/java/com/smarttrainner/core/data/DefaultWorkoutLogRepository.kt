package com.smarttrainner.core.data

import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.WorkoutLog
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultWorkoutLogRepository @Inject constructor(
    private val workoutLogDao: WorkoutLogDao,
    private val activeSessionResolver: ActiveSessionResolver
) : WorkoutLogRepository {
    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            workoutLogDao
                .observeBetween(
                    sessionId = sessionId,
                    startDate = weekStartDate.toString(),
                    endDate = weekStartDate.plusDays(6).toString()
                )
                .map { entities -> entities.map { it.toModel() } }
        }

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            workoutLogDao
                .observeLatestByExerciseForSession(sessionId = sessionId)
                .map { entities -> entities.map { it.toModel() } }
        }

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            workoutLogDao
                .observeAll(sessionId = sessionId)
                .map { entities -> entities.map { it.toModel() } }
        }
}
