package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.WeeklySummary
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface WeeklySummaryRepository {
    fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary>
}
