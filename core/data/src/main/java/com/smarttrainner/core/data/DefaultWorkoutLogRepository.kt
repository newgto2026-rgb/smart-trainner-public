package com.smarttrainner.core.data

import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.database.WorkoutLogEntity
import com.smarttrainner.core.database.WorkoutLogSetWrite
import com.smarttrainner.core.database.WorkoutLogWithSets
import com.smarttrainner.core.database.WorkoutSetLogEntity
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.network.WorkoutLogDto
import com.smarttrainner.core.network.WorkoutLogNetworkApi
import com.smarttrainner.core.network.WorkoutLogRequest
import com.smarttrainner.core.network.WorkoutSetLogDto
import com.smarttrainner.core.network.WorkoutSetLogRequest
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    private val activeSessionResolver: ActiveSessionResolver,
    private val workoutLogNetworkApi: WorkoutLogNetworkApi
) : WorkoutLogRepository, TrainingDataSyncer {
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

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        workoutLogDao.latestByExercise(
            sessionId = activeSessionResolver.sessionId(),
            exerciseId = exerciseId.value
        )?.toModel()

    override suspend fun getLatestWorkoutLog(plannedExerciseId: PlannedExerciseId): WorkoutLog? =
        workoutLogDao.latestByPlannedExercise(
            sessionId = activeSessionResolver.sessionId(),
            plannedExerciseId = plannedExerciseId.value
        )?.toModel()

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val normalizedInput = input.normalized()
        val clientLogId = normalizedInput.clientLogId(sessionId)
        saveNormalizedWorkoutLog(
            sessionId = sessionId,
            clientLogId = clientLogId,
            input = normalizedInput
        )
    }

    override suspend fun updateWorkoutLog(id: WorkoutLogId, input: WorkoutLogInput): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val existing = workoutLogDao.byId(sessionId = sessionId, id = id.value)
            ?: error("Workout log not found: ${id.value}")
        val normalizedInput = input.normalized()
        val clientLogId = existing.log.clientLogId.ifBlank { normalizedInput.clientLogId(sessionId) }
        saveNormalizedWorkoutLog(
            sessionId = sessionId,
            clientLogId = clientLogId,
            input = normalizedInput,
            localId = existing.log.id
        )
    }

    override suspend fun syncPendingTrainingData(): Result<Unit> {
        val sessionId = runCatching { activeSessionResolver.sessionId() }
            .getOrElse { return Result.failure(it) }
        var firstFailure: Throwable? = null
        workoutLogDao.pendingSyncLogs(sessionId).forEach { localLog ->
            runCatching {
                workoutLogNetworkApi.createWorkoutLog(
                    sessionId = sessionId,
                    request = localLog.toNetworkRequest()
                )
            }.onSuccess {
                runCatching {
                    workoutLogDao.markSynced(sessionId, localLog.log.clientLogId)
                }
            }.onFailure { error ->
                if (firstFailure == null) firstFailure = error
            }
        }
        val pendingClientLogIds = workoutLogDao.pendingSyncClientLogIds(sessionId).toSet()
        runCatching {
            val remoteWrites = workoutLogNetworkApi.getWorkoutLogs(sessionId).data
                .filterNot { it.id in pendingClientLogIds }
                .map { remoteLog ->
                    WorkoutLogSetWrite(
                        log = remoteLog.toEntity(),
                        setLogs = remoteLog.sets.toSetEntities()
                    )
                }
            workoutLogDao.upsertAllWithSets(remoteWrites)
        }.onFailure { error ->
            if (firstFailure == null) firstFailure = error
        }
        return firstFailure?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    private suspend fun saveNormalizedWorkoutLog(
        sessionId: String,
        clientLogId: String,
        input: WorkoutLogInput,
        localId: Long = 0L
    ) {
        workoutLogDao.upsertWithSets(
            input.toEntity(sessionId = sessionId, clientLogId = clientLogId).copy(id = localId),
            input.setEntries.toEntities()
        )
        runCatching {
            workoutLogNetworkApi.createWorkoutLog(
                sessionId = sessionId,
                request = input.toNetworkRequest(clientLogId)
            )
        }.onSuccess {
            runCatching {
                workoutLogDao.markSynced(sessionId, clientLogId)
            }
        }
    }
}

internal fun WorkoutLogInput.normalized(): WorkoutLogInput {
    val setEntries = setEntries.ifEmpty {
        List(sets) { index ->
            WorkoutSetLog(
                order = index + 1,
                reps = reps,
                weightKg = weightKg,
                durationMinutes = durationMinutes
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
    return copy(sets = setEntries.size, setEntries = setEntries)
}

internal fun WorkoutLogInput.toEntity(sessionId: String, clientLogId: String): WorkoutLogEntity = WorkoutLogEntity(
    sessionId = sessionId,
    clientLogId = clientLogId,
    plannedExerciseId = plannedExerciseId.value,
    routineDayInstanceId = routineDayInstanceId,
    exerciseId = exerciseId.value,
    performedDate = performedAt.toLocalDate().toString(),
    performedAt = performedAt.toString(),
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    durationMinutes = durationMinutes,
    memo = memo,
    completed = completed
)

internal fun WorkoutLogInput.toNetworkRequest(clientLogId: String): WorkoutLogRequest =
    WorkoutLogRequest(
        id = clientLogId,
        date = performedAt.atZone(ZoneId.systemDefault())
            .toOffsetDateTime()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        exerciseId = exerciseId.value,
        plannedExerciseId = plannedExerciseId.value.takeIf { it.isNotBlank() },
        routineDayInstanceId = routineDayInstanceId,
        notes = memo.takeIf { it.isNotBlank() },
        sets = setEntries.map {
            WorkoutSetLogRequest(
                setIndex = it.order,
                reps = it.reps,
                weightKg = it.weightKg,
                durationMinutes = it.durationMinutes,
                restSeconds = it.restSeconds,
                completed = completed
            )
        }
    )

internal fun WorkoutLogWithSets.toNetworkRequest(): WorkoutLogRequest =
    WorkoutLogRequest(
        id = log.clientLogId,
        date = LocalDateTime.parse(log.performedAt).atZone(ZoneId.systemDefault())
            .toOffsetDateTime()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        exerciseId = log.exerciseId,
        plannedExerciseId = log.plannedExerciseId.takeIf { it.isNotBlank() },
        routineDayInstanceId = log.routineDayInstanceId,
        notes = log.memo.takeIf { it.isNotBlank() },
        sets = setLogs
            .sortedBy { it.setIndex }
            .map {
                WorkoutSetLogRequest(
                    setIndex = it.setIndex,
                    reps = it.reps,
                    weightKg = it.weightKg,
                    durationMinutes = it.durationMinutes,
                    restSeconds = it.restSeconds,
                    completed = log.completed
                )
            }
    )

internal fun WorkoutLogDto.toEntity(): WorkoutLogEntity {
    val performedAtLocal = OffsetDateTime.parse(date).toLocalDateTime()
    return WorkoutLogEntity(
        clientLogId = id,
        sessionId = sessionId,
        plannedExerciseId = plannedExerciseId.orEmpty(),
        routineDayInstanceId = routineDayInstanceId,
        exerciseId = exerciseId,
        performedDate = performedAtLocal.toLocalDate().toString(),
        performedAt = performedAtLocal.toString(),
        sets = sets.size,
        reps = sets.firstOrNull()?.reps,
        weightKg = sets.firstOrNull()?.weightKg,
        durationMinutes = sets.firstOrNull()?.durationMinutes,
        memo = notes.orEmpty(),
        completed = sets.all { it.completed },
        syncPending = false
    )
}

internal fun List<WorkoutSetLogDto>.toSetEntities(workoutLogId: Long = 0): List<WorkoutSetLogEntity> = map {
    WorkoutSetLogEntity(
        workoutLogId = workoutLogId,
        setIndex = it.setIndex,
        reps = it.reps,
        weightKg = it.weightKg,
        durationMinutes = it.durationMinutes,
        restSeconds = it.restSeconds
    )
}

internal fun WorkoutLogInput.clientLogId(sessionId: String): String {
    val input = listOf(
        sessionId,
        plannedExerciseId.value,
        routineDayInstanceId.orEmpty(),
        exerciseId.value,
        performedAt.toString()
    )
        .joinToString(separator = "|")
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { "%02x".format(it) }
    return "android-$digest"
}

internal fun List<WorkoutSetLog>.toEntities(workoutLogId: Long = 0): List<WorkoutSetLogEntity> = map {
    WorkoutSetLogEntity(
        workoutLogId = workoutLogId,
        setIndex = it.order,
        reps = it.reps,
        weightKg = it.weightKg,
        durationMinutes = it.durationMinutes,
        restSeconds = it.restSeconds
    )
}
