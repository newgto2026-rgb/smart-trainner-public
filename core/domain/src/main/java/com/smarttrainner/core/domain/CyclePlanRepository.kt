package com.smarttrainner.core.domain

import com.smarttrainner.core.model.CyclePlan
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface CyclePlanRepository {
    fun observeCurrentCyclePlan(cycleStartDate: LocalDate): Flow<CyclePlan>
}
