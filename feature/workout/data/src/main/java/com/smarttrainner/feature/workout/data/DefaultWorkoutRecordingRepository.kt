package com.smarttrainner.feature.workout.data

import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.database.WorkoutLogEntity
import com.smarttrainner.core.database.WorkoutLogWithSets
import com.smarttrainner.core.database.WorkoutSetLogEntity
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.network.WorkoutLogBackupRequest
import com.smarttrainner.core.network.WorkoutLogNetworkApi
import com.smarttrainner.core.network.WorkoutSetLogBackupRequest
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class DefaultWorkoutRecordingRepository @Inject constructor(
    private val workoutLogDao: WorkoutLogDao,
    private val activeSessionResolver: ActiveSessionResolver,
    private val preferences: TrainingPreferencesDataSource,
    private val workoutLogApi: WorkoutLogNetworkApi
) : WorkoutRecordingRepository {
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
        val normalizedInput = input.copy(sets = setEntries.size, setEntries = setEntries)
        val sessionId = activeSessionResolver.sessionId()
        workoutLogDao.upsertWithSets(normalizedInput.toEntity(sessionId), setEntries.toEntities())

        val activeSession = preferences.activeSession.first()
        if (activeSession?.isLinked == true) {
            workoutLogApi.createWorkoutLog(
                sessionId = sessionId,
                request = normalizedInput.toBackupRequest()
            )
        }
    }
}

internal fun WorkoutLogInput.toEntity(sessionId: String): WorkoutLogEntity = WorkoutLogEntity(
    sessionId = sessionId,
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

internal fun WorkoutLogInput.toBackupRequest(): WorkoutLogBackupRequest = WorkoutLogBackupRequest(
    date = performedAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    exerciseId = exerciseId.value,
    plannedExerciseId = plannedExerciseId.value,
    sets = setEntries
        .sortedBy { it.order }
        .map {
            WorkoutSetLogBackupRequest(
                setIndex = it.order,
                reps = it.reps,
                weightKg = it.weightKg,
                durationMinutes = it.durationMinutes,
                completed = completed
            )
        },
    notes = memo.takeIf { it.isNotBlank() }
)

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
