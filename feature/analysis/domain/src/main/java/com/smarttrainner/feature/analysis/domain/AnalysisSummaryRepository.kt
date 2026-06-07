package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.CycleSummary
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

interface CycleSummaryRepository {
    fun observeCycleSummary(
        currentCycle: CurrentRoutineCycle,
        zone: ZoneId
    ): Flow<CycleSummary>
}
