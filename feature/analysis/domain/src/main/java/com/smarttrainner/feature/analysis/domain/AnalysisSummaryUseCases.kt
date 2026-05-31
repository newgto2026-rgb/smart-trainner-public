package com.smarttrainner.feature.analysis.domain

import java.time.LocalDate
import javax.inject.Inject

class ObserveWeeklySummaryUseCase @Inject constructor(
    private val repository: WeeklySummaryRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeWeeklySummary(weekStartDate)
}
