package com.smarttrainner.core.domain

import com.smarttrainner.core.model.WeeklyPlan
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface WeeklyPlanRepository {
    fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan>
}
