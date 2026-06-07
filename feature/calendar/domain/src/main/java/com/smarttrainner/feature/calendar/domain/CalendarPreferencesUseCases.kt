package com.smarttrainner.feature.calendar.domain

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCalendarMonthExpandedUseCase @Inject constructor(
    private val repository: CalendarPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.observeMonthExpanded()
}

class UpdateCalendarMonthExpandedUseCase @Inject constructor(
    private val repository: CalendarPreferencesRepository
) {
    suspend operator fun invoke(isExpanded: Boolean): Result<Unit> =
        repository.setMonthExpanded(isExpanded)
}
