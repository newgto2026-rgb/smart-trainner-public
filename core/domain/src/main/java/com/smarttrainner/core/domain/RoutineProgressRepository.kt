package com.smarttrainner.core.domain

import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineCycleCompletion
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface RoutineProgressRepository {
    fun observeRoutineProgress(): Flow<RoutineProgress>

    fun observeRoutineCycleCompletions(
        templateId: String? = null
    ): Flow<List<RoutineCycleCompletion>> = flowOf(emptyList())
}

class ObserveRoutineProgressUseCase @Inject constructor(
    private val repository: RoutineProgressRepository
) {
    operator fun invoke() = repository.observeRoutineProgress()
}

class ObserveRoutineCycleCompletionsUseCase @Inject constructor(
    private val repository: RoutineProgressRepository
) {
    operator fun invoke(templateId: String? = null) =
        repository.observeRoutineCycleCompletions(templateId)
}
