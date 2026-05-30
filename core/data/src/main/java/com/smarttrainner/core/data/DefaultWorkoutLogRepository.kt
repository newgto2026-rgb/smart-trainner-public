package com.smarttrainner.core.data

import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
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
                .observeAll(sessionId = sessionId)
                .map { entities ->
                    entities
                        .map { it.toModel() }
                        .distinctBy { it.exerciseId }
                }
        }

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        workoutLogDao.latestByExercise(
            sessionId = activeSessionResolver.sessionId(),
            exerciseId = exerciseId.value
        )?.toModel()

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = runCatching {
        val setEntries = input.setEntries.ifEmpty {
            List(input.sets) { index ->
                WorkoutSetLog(
                    order = index + 1,
                    reps = input.reps,
                    weightKg = input.weightKg,
                    durationMinutes = input.durationMinutes
                )
            }
        }
        require(setEntries.size in 1..12) { "Sets must be between 1 and 12." }
        require(setEntries.map { it.order }.distinct().size == setEntries.size) {
            "Set order values must be unique."
        }
        setEntries.forEach { entry ->
            require(entry.order in 1..12) { "Set order must be between 1 and 12." }
            require(entry.reps != null || entry.durationMinutes != null) {
                "Each set needs reps or duration."
            }
            require(entry.reps?.let { it in 1..50 } ?: true) { "Reps must be between 1 and 50." }
            require(entry.weightKg?.let { it >= 0.0 } ?: true) { "Weight cannot be negative." }
            require(entry.durationMinutes?.let { it in 1..240 } ?: true) {
                "Duration must be between 1 and 240 minutes."
            }
            require(entry.restSeconds?.let { it in 0..600 } ?: true) {
                "Rest must be between 0 and 600 seconds."
            }
        }
        workoutLogDao.upsertWithSets(
            input.copy(sets = setEntries.size, setEntries = setEntries)
                .toEntity(activeSessionResolver.sessionId()),
            setEntries.toEntities()
        )
    }
}
