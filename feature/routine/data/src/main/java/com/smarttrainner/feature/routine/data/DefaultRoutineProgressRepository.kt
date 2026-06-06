package com.smarttrainner.feature.routine.data

import com.smarttrainner.core.database.CustomRoutineDao
import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.TrainingSeedStore
import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineCycleCompletion
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineProgressPreference
import com.smarttrainner.core.model.completedCycleDurationDays as calculateCompletedCycleDurationDays
import com.smarttrainner.core.network.RoutineProgressCancelLatestRequest
import com.smarttrainner.core.network.RoutineCycleCompletionDto
import com.smarttrainner.core.network.RoutineProgressCompleteDayRequest
import com.smarttrainner.core.network.RoutineProgressDto
import com.smarttrainner.core.network.RoutineProgressNetworkApi
import com.smarttrainner.core.network.RoutineProgressStartRequest
import com.smarttrainner.core.network.RoutineProgressSwitchTemplateRequest
import com.smarttrainner.core.network.RoutineProgressSyncRequest
import com.smarttrainner.core.network.RoutineProgressSyncStatus
import com.smarttrainner.feature.routine.domain.RoutineCompletionSnapshot
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoutineProgressRepository @Inject constructor(
    private val customRoutineDao: CustomRoutineDao,
    private val workoutLogDao: WorkoutLogDao,
    private val preferences: TrainingPreferencesDataSource,
    private val activeSessionResolver: ActiveSessionResolver,
    private val seedStore: TrainingSeedStore,
    private val routineProgressNetworkApi: RoutineProgressNetworkApi,
    private val clock: Clock
) : RoutineProgressRepository, RoutineProgressCommandRepository, TrainingDataSyncer {
    override fun observeRoutineProgress(): Flow<RoutineProgress> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            channelFlow {
                launch {
                    runCatching { syncLocalProgressWithServer(sessionId) }
                }
                combine(
                    preferences.activeRoutineProgress(sessionId),
                    customRoutineDao.observeForSession(sessionId)
                ) { preference, customRoutines ->
                    val template = seedStore.templateById(
                        preference.templateId,
                        customRoutines.map { it.toPlanTemplate(seedStore.exercises) }
                    )
                    val startedAt = preference.startedAt.toInstantOrNull()
                    RoutineProgress(
                        templateId = template.id,
                        dayIndex = preference.dayIndex.coerceIn(0, (template.cycleLength - 1).coerceAtLeast(0)),
                        cycleNumber = preference.cycleNumber.coerceAtLeast(1),
                        lastCompletedDayIndex = preference.lastCompletedDayIndex,
                        lastCompletedAt = preference.lastCompletedAt.toInstantOrNull(),
                        lastCompletedCycleNumber = preference.lastCompletedCycleNumber,
                        lastCompletedPreviousCycleStartedAt = preference.lastCompletedPreviousCycleStartedAt.toInstantOrNull(),
                        startedAt = startedAt,
                        cycleStartedAt = preference.effectiveCycleStartedAt(clock.zone).toInstantOrNull() ?: startedAt,
                        routineDayDates = preference.routineDayDates.mapNotNull { (instanceId, rawDate) ->
                            rawDate.toLocalDateOrNull()?.let { instanceId to it }
                        }.toMap()
                    )
                }.collect { send(it) }
            }
        }

    override fun observeRoutineCycleCompletions(
        templateId: String?
    ): Flow<List<RoutineCycleCompletion>> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            preferences.activeRoutineProgress(sessionId)
                .map {
                    RoutineCycleCompletionRefreshKey(
                        cycleNumber = it.cycleNumber,
                        lastCompletedAt = it.lastCompletedAt
                    )
                }
                .distinctUntilChanged()
                .flatMapLatest {
                    flow {
                        emit(fetchRoutineCycleCompletions(sessionId, templateId))
                    }
                }
        }

    override suspend fun startRoutine(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        require(templateExists(sessionId, templateId)) { "Unknown plan template: $templateId" }
        preferences.setSelectedTemplateId(sessionId, templateId)
        preferences.setActiveRoutineTemplate(sessionId, templateId)
        val localProgress = preferences.activeRoutineProgress(sessionId).first()
        pushServerProgress(sessionId) {
            routineProgressNetworkApi.startRoutineProgress(
                sessionId = sessionId,
                request = RoutineProgressStartRequest(
                    templateId = templateId,
                    startedAt = localProgress.startedAt,
                    cycleStartedAt = localProgress.cycleStartedAt
                )
            ).data
        }
    }

    override suspend fun switchRoutineTemplate(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        templateFor(sessionId, templateId)
        val localProgress = preferences.activeRoutineProgress(sessionId).first()
        val currentTemplate = templateFor(sessionId, localProgress.templateId)
        val currentCycleNumber = localProgress.cycleNumber.coerceAtLeast(1)
        val currentCycleStartedAt = localProgress.effectiveCycleStartedAt(clock.zone).toInstantOrNull()
        val currentCycleStartDate = currentCycleStartedAt
            ?.atZone(clock.zone)
            ?.toLocalDate()
            ?: LocalDate.now(clock)
        val currentCyclePlannedExerciseIds = seedStore
            .buildCyclePlan(currentTemplate, currentCycleStartDate)
            .days
            .flatMap { day -> day.exercises.map { it.id.value } }
        val routineDayInstancePrefix = "routine-day|${localProgress.templateId}|cycle$currentCycleNumber|"
        val additionalExercisePrefix = "routine-added|${localProgress.templateId}|cycle$currentCycleNumber|"
        val switchedAt = clock.instant().toString()
        val switchedProgress = localProgress.copy(
            templateId = templateId,
            dayIndex = 0,
            cycleNumber = currentCycleNumber,
            startedAt = switchedAt,
            cycleStartedAt = switchedAt,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            lastCompletedCycleNumber = null,
            lastCompletedPreviousCycleStartedAt = null,
            routineDayDates = emptyMap()
        )
        val serverProgress = try {
            routineProgressNetworkApi.switchRoutineTemplate(
                sessionId = sessionId,
                request = RoutineProgressSwitchTemplateRequest(
                    templateId = templateId,
                    dayIndex = switchedProgress.dayIndex,
                    cycleNumber = switchedProgress.cycleNumber,
                    startedAt = switchedProgress.startedAt,
                    cycleStartedAt = switchedProgress.cycleStartedAt,
                    discardedRoutineDayInstancePrefix = routineDayInstancePrefix,
                    discardedPlannedExerciseIds = currentCyclePlannedExerciseIds,
                    discardedAdditionalExerciseIdPrefix = additionalExercisePrefix
                )
            ).data
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            if (error.code() in 400..499) {
                runCatching { syncLocalProgressWithServer(sessionId) }
            }
            throw error
        } catch (error: Exception) {
            throw IllegalStateException("Server confirmation is required before switching routines.", error)
        }
        workoutLogDao.deleteRoutineCycleLogs(
            sessionId = sessionId,
            routineDayInstancePrefixPattern = "$routineDayInstancePrefix%",
            plannedExerciseIds = currentCyclePlannedExerciseIds,
            additionalExerciseIdPrefixPattern = "$additionalExercisePrefix%"
        )
        val syncedProgress = serverProgress.toPreference().copy(
            dayIndex = switchedProgress.dayIndex,
            cycleNumber = switchedProgress.cycleNumber,
            startedAt = switchedProgress.startedAt,
            cycleStartedAt = switchedProgress.cycleStartedAt,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            lastCompletedCycleNumber = null,
            lastCompletedPreviousCycleStartedAt = null,
            routineDayDates = emptyMap()
        )
        preferences.setRoutineProgress(sessionId, syncedProgress)
        Unit
    }

    override suspend fun setRoutineDayDate(
        routineDayInstanceId: String,
        assignedDate: LocalDate,
        cycleStartedAt: Instant?
    ): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val performedAt = assignedDate.atTime(LocalTime.NOON)
        preferences.setRoutineDayDate(
            sessionId = sessionId,
            routineDayInstanceId = routineDayInstanceId,
            assignedDate = assignedDate.toString(),
            cycleStartedAt = cycleStartedAt?.toString()
        )
        workoutLogDao.updateRoutineDayLogDate(
            sessionId = sessionId,
            routineDayInstanceId = routineDayInstanceId,
            performedDate = assignedDate.toString(),
            performedAt = performedAt.toString()
        )
    }

    override suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val localProgress = preferences.activeRoutineProgress(sessionId).first()
        val cycleDurationDays = if (newCycleStartedAt != null) {
            calculateCompletedCycleDurationDays(
                cycleStartedAt = localProgress.effectiveCycleStartedAt(clock.zone).toInstantOrNull(),
                completedAt = completedAt
            )
        } else {
            null
        }
        preferences.markRoutineDayCompleted(
            sessionId = sessionId,
            completedDayIndex = completedDayIndex,
            nextDayIndex = nextDayIndex,
            completedAt = completedAt.toString(),
            newCycleStartedAt = newCycleStartedAt?.toString()
        )
        pushServerProgress(sessionId) {
            routineProgressNetworkApi.completeRoutineDay(
                sessionId = sessionId,
                request = RoutineProgressCompleteDayRequest(
                    templateId = localProgress.templateId,
                    completedDayIndex = completedDayIndex,
                    nextDayIndex = nextDayIndex,
                    completedAt = completedAt.toString(),
                    newCycleStartedAt = newCycleStartedAt?.toString(),
                    completedCycleDurationDays = cycleDurationDays
                )
            ).data
        }
    }

    override suspend fun cancelLatestRoutineDayCompletion(
        restoredDayIndex: Int,
        restoredCycleNumber: Int,
        restoredCycleStartedAt: Instant?,
        remainingLatestCompletion: RoutineCompletionSnapshot?,
        routineDayInstanceId: String,
        plannedExerciseIds: Set<PlannedExerciseId>,
        additionalExerciseIdPrefix: String
    ): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val localProgress = preferences.activeRoutineProgress(sessionId).first()
        val serverProgress = try {
            routineProgressNetworkApi.cancelLatestRoutineDayCompletion(
                sessionId = sessionId,
                request = RoutineProgressCancelLatestRequest(
                    templateId = localProgress.templateId,
                    restoredDayIndex = restoredDayIndex,
                    restoredCycleNumber = restoredCycleNumber,
                    restoredCycleStartedAt = restoredCycleStartedAt?.toString(),
                    remainingLastCompletedDayIndex = remainingLatestCompletion?.dayIndex,
                    remainingLastCompletedAt = remainingLatestCompletion?.completedAt?.toString(),
                    remainingLastCompletedCycleNumber = remainingLatestCompletion?.cycleNumber,
                    remainingLastCompletedPreviousCycleStartedAt =
                        remainingLatestCompletion?.previousCycleStartedAt?.toString(),
                    routineDayInstanceId = routineDayInstanceId,
                    plannedExerciseIds = plannedExerciseIds.map { it.value },
                    additionalExerciseIdPrefix = additionalExerciseIdPrefix
                )
            ).data.progress
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            if (error.code() in 400..499) {
                runCatching { syncLocalProgressWithServer(sessionId) }
            }
            throw error
        } catch (error: Exception) {
            throw IllegalStateException("Server confirmation is required before deleting routine day logs.", error)
        }
        workoutLogDao.deleteRoutineDayLogs(
            sessionId = sessionId,
            routineDayInstanceId = routineDayInstanceId,
            plannedExerciseIds = plannedExerciseIds.map { it.value },
            additionalExerciseIdPrefixPattern = "$additionalExerciseIdPrefix%"
        )
        preferences.setRoutineProgress(
            sessionId = sessionId,
            progress = serverProgress.toPreference().copy(
                lastCompletedDayIndex = remainingLatestCompletion?.dayIndex,
                lastCompletedAt = remainingLatestCompletion?.completedAt?.toString(),
                lastCompletedCycleNumber = remainingLatestCompletion?.cycleNumber,
                lastCompletedPreviousCycleStartedAt =
                    remainingLatestCompletion?.previousCycleStartedAt?.toString()
            )
        )
    }

    override suspend fun syncPendingTrainingData(): Result<Unit> = runCatching {
        syncLocalProgressWithServer(activeSessionResolver.sessionId())
    }

    private suspend fun templateExists(sessionId: String, templateId: String): Boolean =
        seedStore.hasTemplate(templateId) || customRoutineDao.getById(sessionId, templateId) != null

    private suspend fun templateFor(sessionId: String, templateId: String) =
        seedStore.templateById(
            templateId,
            customRoutineDao.observeForSession(sessionId).first().map { it.toPlanTemplate(seedStore.exercises) }
        )

    private suspend fun pushServerProgress(
        sessionId: String,
        writeToPreferences: Boolean = true,
        request: suspend () -> RoutineProgressDto
    ): RoutineProgressDto? = try {
        val localProgress = preferences.activeRoutineProgress(sessionId).first()
        request().also {
            if (writeToPreferences) {
                preferences.setRoutineProgress(
                    sessionId = sessionId,
                    progress = it.toPreference().withLocalRoutineDayDateState(
                        localProgress = localProgress,
                        zoneId = clock.zone
                    )
                )
            }
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        if (error.code() in 400..499) {
            runCatching { syncLocalProgressWithServer(sessionId) }
            throw error
        }
        null
    } catch (_: Exception) {
        null
    }

    private suspend fun syncLocalProgressWithServer(sessionId: String) {
        val localProgress = preferences.activeRoutineProgress(sessionId).first()
        val result = routineProgressNetworkApi.syncRoutineProgress(
            sessionId = sessionId,
            request = localProgress.toSyncRequest()
        ).data
        if (result.status == RoutineProgressSyncStatus.SERVER_WINS) {
            preferences.setRoutineProgress(
                sessionId = sessionId,
                progress = result.progress.toPreference().withLocalRoutineDayDateState(
                    localProgress = localProgress,
                    zoneId = clock.zone
                )
            )
        }
    }

    private suspend fun fetchRoutineCycleCompletions(
        sessionId: String,
        templateId: String?
    ): List<RoutineCycleCompletion> =
        try {
            routineProgressNetworkApi.getRoutineCycleCompletions(
                sessionId = sessionId,
                templateId = templateId
            ).data.mapNotNull { it.toModel() }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            emptyList()
        }

    private fun RoutineProgressDto.toPreference(): RoutineProgressPreference =
        RoutineProgressPreference(
            templateId = templateId,
            dayIndex = dayIndex,
            cycleNumber = cycleNumber,
            startedAt = startedAt,
            cycleStartedAt = cycleStartedAt,
            lastCompletedDayIndex = lastCompletedDayIndex,
            lastCompletedAt = lastCompletedAt,
            lastCompletedCycleNumber = lastCompletedCycleNumber,
            lastCompletedPreviousCycleStartedAt = lastCompletedPreviousCycleStartedAt
        )

    private fun RoutineCycleCompletionDto.toModel(): RoutineCycleCompletion? {
        val parsedStartedAt = startedAt.toInstantOrNull() ?: return null
        val parsedCompletedAt = completedAt.toInstantOrNull() ?: return null
        return RoutineCycleCompletion(
            id = id,
            templateId = templateId,
            cycleNumber = cycleNumber,
            startedAt = parsedStartedAt,
            completedAt = parsedCompletedAt,
            durationDays = durationDays,
            completedDayIndex = completedDayIndex,
            createdAt = createdAt.toInstantOrNull(),
            updatedAt = updatedAt.toInstantOrNull()
        )
    }

    private fun RoutineProgressPreference.toSyncRequest(): RoutineProgressSyncRequest =
        RoutineProgressSyncRequest(
            templateId = templateId,
            dayIndex = dayIndex,
            cycleNumber = cycleNumber,
            startedAt = startedAt,
            cycleStartedAt = cycleStartedAt,
            lastCompletedDayIndex = lastCompletedDayIndex,
            lastCompletedAt = lastCompletedAt,
            lastCompletedCycleNumber = lastCompletedCycleNumber,
            lastCompletedPreviousCycleStartedAt = lastCompletedPreviousCycleStartedAt
        )

    private fun String?.toInstantOrNull(): Instant? =
        this?.let { raw -> runCatching { Instant.parse(raw) }.getOrNull() }

    private fun String.toLocalDateOrNull(): LocalDate? =
        runCatching { LocalDate.parse(this) }.getOrNull()

}

internal fun RoutineProgressPreference.effectiveCycleStartedAt(zoneId: ZoneId): String? {
    val assignedDayOneStart = routineDayDates["routine-day|$templateId|cycle$cycleNumber|day1"]
        ?.let { rawDate -> runCatching { LocalDate.parse(rawDate) }.getOrNull() }
        ?.atStartOfDay(zoneId)
        ?.toInstant()
        ?: return cycleStartedAt
    val storedCycleStartedAt = cycleStartedAt?.let { rawInstant ->
        runCatching { Instant.parse(rawInstant) }.getOrNull()
    }
    return if (storedCycleStartedAt == null || storedCycleStartedAt.isAfter(assignedDayOneStart)) {
        assignedDayOneStart.toString()
    } else {
        cycleStartedAt
    }
}

internal fun RoutineProgressPreference.withLocalRoutineDayDateState(
    localProgress: RoutineProgressPreference,
    zoneId: ZoneId
): RoutineProgressPreference =
    if (templateId == localProgress.templateId && cycleNumber == localProgress.cycleNumber) {
        val updated = copy(routineDayDates = localProgress.routineDayDates)
        updated.copy(
            cycleStartedAt = updated.effectiveCycleStartedAt(zoneId) ?: cycleStartedAt
        )
    } else {
        this
    }

private data class RoutineCycleCompletionRefreshKey(
    val cycleNumber: Int,
    val lastCompletedAt: String?
)
