package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.core.model.RoutineProgress
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

interface CycleSummaryRepository {
    fun observeCycleSummary(
        progress: RoutineProgress,
        zone: ZoneId
    ): Flow<CycleSummary>
}
