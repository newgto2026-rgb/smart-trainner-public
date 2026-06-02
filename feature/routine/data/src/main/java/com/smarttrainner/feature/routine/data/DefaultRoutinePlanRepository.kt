package com.smarttrainner.feature.routine.data

import com.smarttrainner.core.database.CustomRoutineDao
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.TrainingSeedStore
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoutinePlanRepository @Inject constructor(
    private val customRoutineDao: CustomRoutineDao,
    private val preferences: TrainingPreferencesDataSource,
    private val activeSessionResolver: ActiveSessionResolver,
    private val seedStore: TrainingSeedStore,
    private val clock: Clock
) : RoutinePlanCatalogRepository, RoutinePlanCommandRepository {
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
        requireNotNull(customRoutineDao.getById(sessionId, routineId)).toPlanTemplate(seedStore.exercises)
    }

    override suspend fun deleteCustomRoutine(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val deleted = customRoutineDao.deleteRoutine(sessionId, templateId)
        require(deleted > 0) { "Unknown custom routine: $templateId" }
        val selectedTemplateId = preferences.selectedTemplateId(sessionId).first()
        val activeTemplateId = preferences.activeRoutineProgress(sessionId).first().templateId
        if (selectedTemplateId == templateId || activeTemplateId == templateId) {
            val fallbackTemplateId = seedStore.templates.first().id
            preferences.setSelectedTemplateId(sessionId, fallbackTemplateId)
            preferences.setActiveRoutineTemplate(sessionId, fallbackTemplateId)
        }
    }

    private suspend fun templateExists(sessionId: String, templateId: String): Boolean =
        seedStore.hasTemplate(templateId) || customRoutineDao.getById(sessionId, templateId) != null
}
