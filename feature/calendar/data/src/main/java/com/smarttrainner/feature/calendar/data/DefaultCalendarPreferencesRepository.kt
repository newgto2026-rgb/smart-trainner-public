package com.smarttrainner.feature.calendar.data

import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.feature.calendar.domain.CalendarPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class DefaultCalendarPreferencesRepository @Inject constructor(
    private val preferences: TrainingPreferencesDataSource
) : CalendarPreferencesRepository {
    override fun observeMonthExpanded(): Flow<Boolean> =
        preferences.calendarMonthExpanded

    override suspend fun setMonthExpanded(isExpanded: Boolean): Result<Unit> =
        runCatching {
            preferences.setCalendarMonthExpanded(isExpanded)
        }.onFailure { throwable ->
            if (throwable is CancellationException) throw throwable
        }
}
