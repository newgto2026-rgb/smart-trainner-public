package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.domain.WeeklyPlanRepository
import java.time.LocalDate
import javax.inject.Inject

class ObserveCurrentWeeklyPlanUseCase @Inject constructor(
    private val repository: WeeklyPlanRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeCurrentWeeklyPlan(weekStartDate)
}

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
