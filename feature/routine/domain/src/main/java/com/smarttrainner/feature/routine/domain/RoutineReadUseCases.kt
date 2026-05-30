package com.smarttrainner.feature.routine.domain

import javax.inject.Inject

class ObservePlanTemplatesUseCase @Inject constructor(
    private val repository: RoutinePlanCatalogRepository
) {
    operator fun invoke() = repository.observePlanTemplates()
}

class ObserveRoutineProgressUseCase @Inject constructor(
    private val repository: RoutineProgressRepository
) {
    operator fun invoke() = repository.observeRoutineProgress()
}
