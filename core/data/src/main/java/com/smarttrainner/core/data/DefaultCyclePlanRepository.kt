package com.smarttrainner.core.data

import com.smarttrainner.core.database.CustomRoutineDao
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.TrainingSeedStore
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.model.CyclePlan
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCyclePlanRepository @Inject constructor(
    private val customRoutineDao: CustomRoutineDao,
    private val preferences: TrainingPreferencesDataSource,
    private val activeSessionResolver: ActiveSessionResolver,
    private val seedStore: TrainingSeedStore
) : CyclePlanRepository {
    override fun observeCurrentCyclePlan(cycleStartDate: LocalDate): Flow<CyclePlan> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            combine(
                preferences.selectedTemplateId(sessionId),
                customRoutineDao.observeForSession(sessionId)
            ) { templateId, customRoutines ->
                val customTemplates = customRoutines.map { it.toPlanTemplate(seedStore.exercises) }
                seedStore.buildCyclePlan(
                    template = seedStore.templateById(templateId, customTemplates),
                    cycleStartDate = cycleStartDate
                )
            }
        }
}
