package com.smarttrainner.core.data

import com.smarttrainner.core.database.CUSTOM_EXERCISE_SYNCED
import com.smarttrainner.core.database.CUSTOM_EXERCISE_SYNC_PENDING_DELETE
import com.smarttrainner.core.database.CustomExerciseDao
import com.smarttrainner.core.database.CustomExerciseEntity
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.TrainingSeedStore
import com.smarttrainner.core.model.CustomExerciseInput
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseSource
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.network.CustomExerciseDto
import com.smarttrainner.core.network.CustomExerciseNetworkApi
import com.smarttrainner.core.network.CustomExerciseRequest
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultExerciseRepository @Inject constructor(
    private val seedStore: TrainingSeedStore,
    private val customExerciseDao: CustomExerciseDao,
    private val activeSessionResolver: ActiveSessionResolver,
    private val customExerciseNetworkApi: CustomExerciseNetworkApi,
    private val clock: Clock
) : ExerciseRepository, TrainingDataSyncer {
    override val syncPriority: Int = 20

    override fun observeExercises(): Flow<List<Exercise>> =
        activeSessionResolver.observeSessionId().flatMapLatest { ownerSessionId ->
            customExerciseDao.observeActiveForOwner(ownerSessionId).map { customExercises ->
                seedStore.exercises + customExercises.map { it.toExercise() }
            }
        }

    override suspend fun getExercise(id: ExerciseId): Exercise? =
        seedStore.exercise(id) ?: customExerciseDao
            .getById(activeSessionResolver.sessionId(), id.value)
            ?.toExercise()

    override suspend fun saveCustomExercise(input: CustomExerciseInput): Result<Exercise> = runCatching {
        val ownerSessionId = activeSessionResolver.sessionId()
        val now = clock.instant()
        val existing = input.id?.let { customExerciseDao.getById(ownerSessionId, it.value) }
        val exerciseId = input.id?.value?.takeIf { it.isNotBlank() } ?: nextCustomExerciseId()
        require(seedStore.exercise(ExerciseId(exerciseId)) == null) { "Exercise ID already exists: $exerciseId" }
        require(existing != null || customExerciseDao.countById(ownerSessionId, exerciseId) == 0) {
            "Custom exercise ID already exists: $exerciseId"
        }
        val entity = input.toEntity(
            id = exerciseId,
            ownerSessionId = ownerSessionId,
            createdAt = existing?.createdAt ?: now.toString(),
            updatedAt = now.toString(),
            archivedAt = existing?.archivedAt
        )
        customExerciseDao.upsert(entity)
        runCatching {
            val request = entity.toNetworkRequest()
            if (existing == null) {
                customExerciseNetworkApi.createCustomExercise(ownerSessionId, request)
            } else {
                customExerciseNetworkApi.updateCustomExercise(exerciseId, ownerSessionId, request)
            }
        }.onSuccess {
            customExerciseDao.updateSyncState(ownerSessionId, exerciseId, CUSTOM_EXERCISE_SYNCED)
        }
        requireNotNull(customExerciseDao.getById(ownerSessionId, exerciseId)).toExercise()
    }

    override suspend fun archiveCustomExercise(id: ExerciseId): Result<Unit> = runCatching {
        val ownerSessionId = activeSessionResolver.sessionId()
        require(customExerciseDao.markPendingArchive(ownerSessionId, id.value, clock.instant().toString()) > 0) {
            "Unknown custom exercise: ${id.value}"
        }
        runCatching {
            customExerciseNetworkApi.archiveCustomExercise(id.value, ownerSessionId)
        }.onSuccess {
            customExerciseDao.updateSyncState(ownerSessionId, id.value, CUSTOM_EXERCISE_SYNCED)
        }
    }

    override suspend fun syncPendingTrainingData(): Result<Unit> {
        val ownerSessionId = runCatching { activeSessionResolver.sessionId() }
            .getOrElse { return Result.failure(it) }
        var firstFailure: Throwable? = null
        customExerciseDao.pendingSyncForOwner(ownerSessionId).forEach { pending ->
            runCatching {
                if (pending.syncState == CUSTOM_EXERCISE_SYNC_PENDING_DELETE || pending.archivedAt != null) {
                    customExerciseNetworkApi.archiveCustomExercise(pending.id, ownerSessionId)
                } else {
                    customExerciseNetworkApi.createCustomExercise(ownerSessionId, pending.toNetworkRequest())
                }
                customExerciseDao.updateSyncState(ownerSessionId, pending.id, CUSTOM_EXERCISE_SYNCED)
            }.onFailure { error ->
                if (firstFailure == null) firstFailure = error
            }
        }
        runCatching {
            customExerciseNetworkApi.getCustomExercises(ownerSessionId).data.forEach { dto ->
                val local = customExerciseDao.getById(ownerSessionId, dto.id)
                if (local == null || local.syncState == CUSTOM_EXERCISE_SYNCED) {
                    customExerciseDao.upsert(dto.toEntity())
                }
            }
        }.onFailure { error ->
            if (firstFailure == null) firstFailure = error
        }
        return firstFailure?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    private fun nextCustomExerciseId(): String =
        "custom_exercise_${UUID.randomUUID().toString().replace("-", "_")}"
}

internal fun CustomExerciseInput.toEntity(
    id: String,
    ownerSessionId: String,
    createdAt: String,
    updatedAt: String,
    archivedAt: String?
): CustomExerciseEntity {
    val cleanedInstructions = instructions.map { it.trim() }.filter { it.isNotEmpty() }
    val cleanedSafetyCues = safetyCues.map { it.trim() }.filter { it.isNotEmpty() }
    val resolvedSummary = summary.trim().ifEmpty { "${name.trim()} 운동입니다." }
    return CustomExerciseEntity(
        id = id,
        ownerSessionId = ownerSessionId,
        source = ExerciseSource.USER_CREATED.name,
        originExerciseId = null,
        name = name.trim(),
        primaryMuscleGroup = muscleGroup.name,
        secondaryMuscleGroups = "",
        equipment = equipment.name,
        difficulty = difficulty.name,
        imageKey = id,
        imageUri = imageUri?.trim()?.takeIf { it.isNotEmpty() },
        summary = resolvedSummary,
        instructions = cleanedInstructions.joinToString("\n"),
        safetyCues = cleanedSafetyCues.joinToString("\n"),
        defaultSets = defaultSets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        defaultDurationMinutes = defaultDurationMinutes,
        restSeconds = restSeconds,
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt
    )
}

internal fun CustomExerciseEntity.toExercise(): Exercise {
    val primaryGroup = enumValueOrDefault(primaryMuscleGroup, MuscleGroup.FULL_BODY)
    val secondaryGroups = secondaryMuscleGroups.split(",")
        .mapNotNull { raw -> raw.takeIf { it.isNotBlank() }?.let { enumValueOrNull<MuscleGroup>(it) } }
    val sourceValue = enumValueOrDefault(source, ExerciseSource.USER_CREATED)
    val repStart = repRangeStart
    val repEnd = repRangeEnd
    val repRange = if (repStart != null && repEnd != null) {
        repStart..repEnd
    } else {
        null
    }
    return Exercise(
        id = ExerciseId(id),
        name = name,
        muscleGroup = primaryGroup,
        muscleGroups = (listOf(primaryGroup) + secondaryGroups).distinct(),
        equipment = enumValueOrDefault(equipment, EquipmentType.BODYWEIGHT),
        difficulty = enumValueOrDefault(difficulty, DifficultyLevel.BEGINNER),
        imageKey = imageKey,
        summary = summary,
        instructions = instructions.lines().filter { it.isNotBlank() },
        safetyCues = safetyCues.lines().filter { it.isNotBlank() },
        defaultSets = defaultSets,
        defaultRepRange = repRange,
        defaultDurationMinutes = defaultDurationMinutes,
        restSeconds = restSeconds,
        source = sourceValue,
        ownerSessionId = UserSessionId(ownerSessionId),
        originExerciseId = originExerciseId?.let(::ExerciseId),
        imageUri = imageUri,
        createdAt = createdAt.toInstantOrNull(),
        updatedAt = updatedAt.toInstantOrNull(),
        archivedAt = archivedAt?.toInstantOrNull()
    )
}

internal fun CustomExerciseEntity.toNetworkRequest(): CustomExerciseRequest =
    CustomExerciseRequest(
        id = id,
        name = name,
        muscleGroup = primaryMuscleGroup,
        muscleGroups = (listOf(primaryMuscleGroup) + secondaryMuscleGroups.split(","))
            .filter { it.isNotBlank() }
            .distinct(),
        equipment = equipment,
        difficulty = difficulty,
        imageUrl = imageUri?.takeIf { it.startsWith("http://") || it.startsWith("https://") },
        summary = summary,
        instructions = instructions.lines().filter { it.isNotBlank() },
        safetyCues = safetyCues.lines().filter { it.isNotBlank() },
        defaultSets = defaultSets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        defaultDurationMinutes = defaultDurationMinutes,
        restSeconds = restSeconds,
        source = source,
        originExerciseId = originExerciseId,
        archivedAt = archivedAt
    )

internal fun CustomExerciseDto.toEntity(): CustomExerciseEntity =
    CustomExerciseEntity(
        id = id,
        ownerSessionId = ownerSessionId,
        source = source,
        originExerciseId = originExerciseId,
        name = name,
        primaryMuscleGroup = muscleGroup,
        secondaryMuscleGroups = muscleGroups.filterNot { it == muscleGroup }.joinToString(","),
        equipment = equipment,
        difficulty = difficulty,
        imageKey = id,
        imageUri = imageUrl,
        summary = summary.orEmpty(),
        instructions = instructions.joinToString("\n"),
        safetyCues = safetyCues.joinToString("\n"),
        defaultSets = defaultSets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        defaultDurationMinutes = defaultDurationMinutes,
        restSeconds = restSeconds,
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt,
        syncState = CUSTOM_EXERCISE_SYNCED
    )

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, defaultValue: T): T =
    enumValueOrNull<T>(value) ?: defaultValue

private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
    runCatching { enumValueOf<T>(value) }.getOrNull()

private fun String.toInstantOrNull(): Instant? =
    runCatching { Instant.parse(this) }.getOrNull()
