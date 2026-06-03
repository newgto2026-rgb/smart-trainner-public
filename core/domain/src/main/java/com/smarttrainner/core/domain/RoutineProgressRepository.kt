package com.smarttrainner.core.domain

import com.smarttrainner.core.model.RoutineProgress
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface RoutineProgressRepository {
    fun observeRoutineProgress(): Flow<RoutineProgress>
}

class ObserveRoutineProgressUseCase @Inject constructor(
    private val repository: RoutineProgressRepository
) {
    operator fun invoke() = repository.observeRoutineProgress()
}
