package com.smarttrainner.core.domain

import javax.inject.Inject

class ObserveRoutineProgressUseCase @Inject constructor(
    private val repository: RoutineProgressRepository
) {
    operator fun invoke() = repository.observeRoutineProgress()
}
