package com.smarttrainner.feature.calendar.domain

import kotlinx.coroutines.flow.Flow

interface CalendarPreferencesRepository {
    fun observeMonthExpanded(): Flow<Boolean>
    suspend fun setMonthExpanded(isExpanded: Boolean): Result<Unit>
}
