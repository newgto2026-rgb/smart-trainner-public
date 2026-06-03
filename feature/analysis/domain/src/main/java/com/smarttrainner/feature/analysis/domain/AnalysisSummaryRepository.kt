package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.RoutineProgress
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

interface WeeklySummaryRepository {
    fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary>
    fun observeCycleSummary(
        weekStartDate: LocalDate,
        progress: RoutineProgress,
        zone: ZoneId
    ): Flow<WeeklySummary>
}
