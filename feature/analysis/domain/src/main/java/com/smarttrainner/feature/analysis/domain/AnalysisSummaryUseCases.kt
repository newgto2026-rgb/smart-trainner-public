package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.RoutineProgress
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ObserveWeeklySummaryUseCase @Inject constructor(
    private val repository: WeeklySummaryRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeWeeklySummary(weekStartDate)
}

class ObserveCycleSummaryUseCase @Inject constructor(
    private val repository: WeeklySummaryRepository
) {
    operator fun invoke(
        weekStartDate: LocalDate,
        progress: RoutineProgress,
        zone: ZoneId
    ) = repository.observeCycleSummary(
        weekStartDate = weekStartDate,
        progress = progress,
        zone = zone
    )
}
