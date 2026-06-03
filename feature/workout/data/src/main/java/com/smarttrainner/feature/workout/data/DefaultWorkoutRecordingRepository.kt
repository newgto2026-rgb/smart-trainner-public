package com.smarttrainner.feature.workout.data

import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.database.WorkoutLogEntity
import com.smarttrainner.core.database.WorkoutLogWithSets
import com.smarttrainner.core.database.WorkoutSetLogEntity
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.network.WorkoutLogNetworkApi
import com.smarttrainner.core.network.WorkoutLogRequest
import com.smarttrainner.core.network.WorkoutSetLogRequest
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWorkoutRecordingRepository @Inject constructor(
    private val workoutLogDao: WorkoutLogDao,
    private val activeSessionResolver: ActiveSessionResolver,
    private val workoutLogNetworkApi: WorkoutLogNetworkApi
) : WorkoutRecordingRepository {
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
        val sessionId = activeSessionResolver.sessionId()
        val normalizedInput = input.copy(sets = setEntries.size, setEntries = setEntries)
        val clientLogId = normalizedInput.clientLogId(sessionId)
        workoutLogNetworkApi.createWorkoutLog(
            sessionId = sessionId,
            request = normalizedInput.toNetworkRequest(clientLogId)
        )
        workoutLogDao.upsertWithSets(
            normalizedInput.toEntity(sessionId = sessionId, clientLogId = clientLogId),
            setEntries.toEntities()
        )
    }
}

internal fun WorkoutLogInput.toEntity(sessionId: String, clientLogId: String): WorkoutLogEntity = WorkoutLogEntity(
    sessionId = sessionId,
    clientLogId = clientLogId,
    plannedExerciseId = plannedExerciseId.value,
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
        plannedExerciseId = plannedExerciseId.value,
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

internal fun WorkoutLogInput.clientLogId(sessionId: String): String {
    val input = listOf(sessionId, plannedExerciseId.value, exerciseId.value, performedAt.toString())
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

internal fun WorkoutLogWithSets.toModel(): WorkoutLog {
    val legacySetEntries = List(log.sets.coerceAtLeast(0)) { index ->
        WorkoutSetLog(
            order = index + 1,
            reps = log.reps,
            weightKg = log.weightKg,
            durationMinutes = log.durationMinutes,
            restSeconds = null
        )
    }
    val setEntries = setLogs
        .sortedBy { it.setIndex }
        .map {
            WorkoutSetLog(
                order = it.setIndex,
                reps = it.reps,
                weightKg = it.weightKg,
                durationMinutes = it.durationMinutes,
                restSeconds = it.restSeconds
            )
        }
        .ifEmpty { legacySetEntries }

    return WorkoutLog(
        id = WorkoutLogId(log.id),
        sessionId = UserSessionId(log.sessionId),
        plannedExerciseId = PlannedExerciseId(log.plannedExerciseId),
        exerciseId = ExerciseId(log.exerciseId),
        performedAt = LocalDateTime.parse(log.performedAt),
        sets = log.sets,
        reps = log.reps,
        weightKg = log.weightKg,
        durationMinutes = log.durationMinutes,
        memo = log.memo,
        completed = log.completed,
        setEntries = setEntries
    )
}
