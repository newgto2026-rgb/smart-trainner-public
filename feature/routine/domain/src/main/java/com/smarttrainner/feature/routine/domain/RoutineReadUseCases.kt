package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.domain.CyclePlanRepository
import java.time.LocalDate
import javax.inject.Inject

class ObserveCurrentCyclePlanUseCase @Inject constructor(
    private val repository: CyclePlanRepository
) {
    operator fun invoke(cycleStartDate: LocalDate) = repository.observeCurrentCyclePlan(cycleStartDate)
}

class ObservePlanTemplatesUseCase @Inject constructor(
    private val repository: RoutinePlanCatalogRepository
) {
    operator fun invoke() = repository.observePlanTemplates()
}
