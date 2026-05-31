package com.smarttrainner.feature.routine.data

import com.smarttrainner.core.database.CustomRoutineDao
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.TrainingSeedStore
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoutineProgressRepository @Inject constructor(
    private val customRoutineDao: CustomRoutineDao,
    private val preferences: TrainingPreferencesDataSource,
    private val activeSessionResolver: ActiveSessionResolver,
    private val seedStore: TrainingSeedStore
) : RoutineProgressRepository, RoutineProgressCommandRepository {
    override fun observeRoutineProgress(): Flow<RoutineProgress> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            combine(
                preferences.activeRoutineProgress(sessionId),
                customRoutineDao.observeForSession(sessionId)
            ) { preference, customRoutines ->
                val template = seedStore.templateById(preference.templateId, customRoutines.map { it.toPlanTemplate() })
                val startedAt = preference.startedAt.toInstantOrNull()
                RoutineProgress(
                    templateId = template.id,
                    dayIndex = preference.dayIndex.coerceIn(0, (template.cycleLength - 1).coerceAtLeast(0)),
                    lastCompletedDayIndex = preference.lastCompletedDayIndex,
                    lastCompletedAt = preference.lastCompletedAt.toInstantOrNull(),
                    startedAt = startedAt,
                    cycleStartedAt = preference.cycleStartedAt.toInstantOrNull() ?: startedAt
                )
            }
        }

    override suspend fun startRoutine(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        require(templateExists(sessionId, templateId)) { "Unknown plan template: $templateId" }
        preferences.setSelectedTemplateId(sessionId, templateId)
        preferences.setActiveRoutineTemplate(sessionId, templateId)
    }

    override suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit> = runCatching {
        preferences.markRoutineDayCompleted(
            sessionId = activeSessionResolver.sessionId(),
            completedDayIndex = completedDayIndex,
            nextDayIndex = nextDayIndex,
            completedAt = completedAt.toString(),
            newCycleStartedAt = newCycleStartedAt?.toString()
        )
    }

    private suspend fun templateExists(sessionId: String, templateId: String): Boolean =
        seedStore.hasTemplate(templateId) || customRoutineDao.getById(sessionId, templateId) != null

    private fun String?.toInstantOrNull(): Instant? =
        this?.let { raw -> runCatching { Instant.parse(raw) }.getOrNull() }
}
