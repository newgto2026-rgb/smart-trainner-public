package com.smarttrainner.feature.routine.data

import com.smarttrainner.core.database.CustomRoutineDao
import com.smarttrainner.core.database.CustomRoutineDayEntity
import com.smarttrainner.core.database.CustomRoutineDayWrite
import com.smarttrainner.core.database.CustomRoutineEntity
import com.smarttrainner.core.database.CustomRoutineExerciseEntity
import com.smarttrainner.core.database.CustomRoutineWithDays
import com.smarttrainner.core.database.CUSTOM_ROUTINE_SYNC_PENDING_DELETE
import com.smarttrainner.core.database.CUSTOM_ROUTINE_SYNCED
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.domain.TrainingSeedStore
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.network.CustomRoutineDayDto
import com.smarttrainner.core.network.CustomRoutineDayRequest
import com.smarttrainner.core.network.CustomRoutineDto
import com.smarttrainner.core.network.CustomRoutineExerciseDto
import com.smarttrainner.core.network.CustomRoutineExerciseRequest
import com.smarttrainner.core.network.CustomRoutineRequest
import com.smarttrainner.core.network.RoutineNetworkApi
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoutinePlanRepository @Inject constructor(
    private val customRoutineDao: CustomRoutineDao,
    private val preferences: TrainingPreferencesDataSource,
    private val activeSessionResolver: ActiveSessionResolver,
    private val seedStore: TrainingSeedStore,
    private val clock: Clock,
    private val routineNetworkApi: RoutineNetworkApi
) : RoutinePlanCatalogRepository, RoutinePlanCommandRepository, TrainingDataSyncer {
    override fun observePlanTemplates(): Flow<List<PlanTemplate>> =
        observeCustomRoutines().map { customTemplates -> seedStore.templates + customTemplates }

    private fun observeCustomRoutines(): Flow<List<PlanTemplate>> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            customRoutineDao.observeForSession(sessionId)
                .map { routines -> routines.map { it.toPlanTemplate(seedStore.exercises) } }
        }

    override suspend fun selectPlanTemplate(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        require(templateExists(sessionId, templateId)) { "Unknown plan template: $templateId" }
        preferences.setSelectedTemplateId(sessionId, templateId)
        if (!seedStore.hasTemplate(templateId)) {
            runCatching { routineNetworkApi.selectCustomRoutine(sessionId, templateId) }
        }
    }

    override suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val existing = input.id?.let { customRoutineDao.getById(sessionId, it) }
        val routineId = input.id?.takeIf { it.isNotBlank() } ?: "custom-${UUID.randomUUID()}"
        val now = clock.instant().toString()
        customRoutineDao.upsertFull(
            routine = input.toEntity(
                routineId = routineId,
                sessionId = sessionId,
                createdAt = existing?.routine?.createdAt ?: now,
                updatedAt = now
            ),
            days = input.toDayWrites(routineId)
        )
        runCatching {
            val request = input.toNetworkRequest(routineId)
            routineNetworkApi.createCustomRoutine(sessionId, request)
        }.onSuccess {
            customRoutineDao.updateSyncState(sessionId, routineId, CUSTOM_ROUTINE_SYNCED)
        }
        requireNotNull(customRoutineDao.getById(sessionId, routineId)).toPlanTemplate(seedStore.exercises)
    }

    override suspend fun deleteCustomRoutine(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        requireNotNull(customRoutineDao.getById(sessionId, templateId)) { "Unknown custom routine: $templateId" }
        val selectedTemplateId = preferences.selectedTemplateId(sessionId).first()
        val activeTemplateId = preferences.activeRoutineProgress(sessionId).first().templateId
        if (selectedTemplateId == templateId || activeTemplateId == templateId) {
            val fallbackTemplateId = seedStore.templates.first().id
            preferences.setSelectedTemplateId(sessionId, fallbackTemplateId)
            preferences.setActiveRoutineTemplate(sessionId, fallbackTemplateId)
        }
        val remoteDeleted = runCatching { deleteRemoteCustomRoutine(sessionId, templateId) }.isSuccess
        if (remoteDeleted) {
            customRoutineDao.deleteRoutine(sessionId, templateId)
        } else {
            customRoutineDao.markPendingDelete(sessionId, templateId, clock.instant().toString())
        }
    }

    override suspend fun syncPendingTrainingData(): Result<Unit> {
        val sessionId = runCatching { activeSessionResolver.sessionId() }
            .getOrElse { return Result.failure(it) }
        var firstFailure: Throwable? = null
        customRoutineDao.pendingSyncForSession(sessionId).forEach { pending ->
            runCatching {
                when (pending.routine.syncState) {
                    CUSTOM_ROUTINE_SYNC_PENDING_DELETE -> {
                        deleteRemoteCustomRoutine(sessionId, pending.routine.id)
                        customRoutineDao.deleteRoutine(sessionId, pending.routine.id)
                    }
                    else -> {
                        routineNetworkApi.createCustomRoutine(
                            sessionId = sessionId,
                            request = pending.toNetworkRequest()
                        )
                        customRoutineDao.updateSyncState(
                            sessionId = sessionId,
                            routineId = pending.routine.id,
                            syncState = CUSTOM_ROUTINE_SYNCED
                        )
                    }
                }
            }.onFailure { error ->
                if (firstFailure == null) firstFailure = error
            }
        }
        runCatching { syncSelectedCustomRoutine(sessionId) }
            .onFailure { error -> if (firstFailure == null) firstFailure = error }
        runCatching { syncCustomRoutinesFromServer(sessionId) }
            .onFailure { error -> if (firstFailure == null) firstFailure = error }
        return firstFailure?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    private suspend fun templateExists(sessionId: String, templateId: String): Boolean =
        seedStore.hasTemplate(templateId) || customRoutineDao.getById(sessionId, templateId) != null

    private suspend fun syncCustomRoutinesFromServer(sessionId: String) {
        val response = routineNetworkApi.getCustomRoutines(sessionId)
        response.data.forEach { routine ->
            val localSyncState = customRoutineDao.syncStateForId(sessionId, routine.id)
            if (localSyncState == null || localSyncState == CUSTOM_ROUTINE_SYNCED) {
                customRoutineDao.upsertFull(
                    routine = routine.toEntity(),
                    days = routine.toDayWrites()
                )
            }
        }
    }

    private suspend fun syncSelectedCustomRoutine(sessionId: String) {
        val selectedTemplateId = preferences.selectedTemplateId(sessionId).first()
        if (seedStore.hasTemplate(selectedTemplateId)) return
        if (customRoutineDao.getById(sessionId, selectedTemplateId) == null) return
        routineNetworkApi.selectCustomRoutine(sessionId, selectedTemplateId)
    }

    private suspend fun deleteRemoteCustomRoutine(sessionId: String, routineId: String) {
        try {
            routineNetworkApi.deleteCustomRoutine(sessionId, routineId)
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            if (error.code() != 404) throw error
        }
    }
}

internal fun CustomRoutineInput.toNetworkRequest(routineId: String): CustomRoutineRequest =
    CustomRoutineRequest(
        id = routineId,
        name = name.trim(),
        description = description.trim().takeIf { it.isNotEmpty() },
        days = days.mapIndexed { dayIndex, day ->
            CustomRoutineDayRequest(
                day = dayIndex + 1,
                title = day.title.trim().takeIf { it.isNotEmpty() },
                focus = day.focus.trim().ifEmpty { day.primaryFocus?.name.orEmpty() },
                primaryFocus = day.primaryFocus?.name,
                secondaryFocuses = day.secondaryFocuses.map { it.name },
                minRecoveryHours = day.minRecoveryHours,
                exercises = day.exercises.map { exercise ->
                    CustomRoutineExerciseRequest(
                        exerciseId = exercise.exerciseId.value,
                        sets = exercise.sets,
                        repRangeStart = exercise.repRangeStart,
                        repRangeEnd = exercise.repRangeEnd,
                        durationMinutes = exercise.durationMinutes,
                        restSeconds = exercise.restSeconds,
                        note = exercise.note.takeIf { it.isNotBlank() }
                    )
                }
            )
        }
    )

internal fun CustomRoutineDto.toEntity(): CustomRoutineEntity =
    CustomRoutineEntity(
        id = id,
        sessionId = sessionId,
        name = name,
        description = description.orEmpty(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = CUSTOM_ROUTINE_SYNCED
    )

internal fun CustomRoutineWithDays.toNetworkRequest(): CustomRoutineRequest =
    CustomRoutineRequest(
        id = routine.id,
        name = routine.name,
        description = routine.description.takeIf { it.isNotBlank() },
        days = days.sortedBy { it.day.dayIndex }.map { day ->
            CustomRoutineDayRequest(
                day = day.day.dayIndex + 1,
                title = day.day.title.takeIf { it.isNotBlank() },
                focus = day.day.focus,
                primaryFocus = day.day.primaryFocus.takeIf { it.isNotBlank() },
                secondaryFocuses = day.day.secondaryFocuses
                    .split(",")
                    .filter { it.isNotBlank() },
                minRecoveryHours = day.day.minRecoveryHours,
                exercises = day.exercises.sortedBy { it.slotIndex }.map { exercise ->
                    CustomRoutineExerciseRequest(
                        exerciseId = exercise.exerciseId,
                        sets = exercise.sets,
                        repRangeStart = exercise.repRangeStart,
                        repRangeEnd = exercise.repRangeEnd,
                        durationMinutes = exercise.durationMinutes,
                        restSeconds = exercise.restSeconds,
                        note = exercise.note.takeIf { it.isNotBlank() }
                    )
                }
            )
        }
    )

internal fun CustomRoutineDto.toDayWrites(): List<CustomRoutineDayWrite> =
    days.sortedBy { it.day }.map { day ->
        val dayIndex = (day.day - 1).coerceAtLeast(0)
        val dayId = "$id-day-${dayIndex + 1}"
        CustomRoutineDayWrite(
            day = day.toEntity(
                id = dayId,
                routineId = id,
                dayIndex = dayIndex
            ),
            exercises = day.exercises.mapIndexed { slotIndex, exercise ->
                exercise.toEntity(
                    id = "$dayId-slot-${slotIndex + 1}",
                    dayId = dayId,
                    slotIndex = slotIndex
                )
            }
        )
    }

private fun CustomRoutineDayDto.toEntity(
    id: String,
    routineId: String,
    dayIndex: Int
): CustomRoutineDayEntity =
    CustomRoutineDayEntity(
        id = id,
        routineId = routineId,
        dayIndex = dayIndex,
        title = title.orEmpty(),
        focus = focus,
        primaryFocus = primaryFocus.orEmpty(),
        secondaryFocuses = secondaryFocuses.joinToString(","),
        minRecoveryHours = minRecoveryHours
    )

private fun CustomRoutineExerciseDto.toEntity(
    id: String,
    dayId: String,
    slotIndex: Int
): CustomRoutineExerciseEntity =
    CustomRoutineExerciseEntity(
        id = id,
        dayId = dayId,
        slotIndex = slotIndex,
        exerciseId = exerciseId,
        sets = sets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        durationMinutes = durationMinutes,
        restSeconds = restSeconds,
        note = note.orEmpty()
    )
